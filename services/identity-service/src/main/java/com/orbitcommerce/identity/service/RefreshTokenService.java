package com.orbitcommerce.identity.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.orbitcommerce.identity.dto.RotateRefreshTokenDTO;
import com.orbitcommerce.identity.enums.UserStatus;
import com.orbitcommerce.identity.model.RefreshToken;
import com.orbitcommerce.identity.repository.RefreshTokenRepository;
import com.orbitcommerce.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${api.security.token.refresh.expiration-time}")
    private long refreshTokenExpiration;

    @Value("${api.security.token.refresh.prefix-blacklist}")
    private String prefixBlacklist;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;

    public RefreshTokenService(
            @Value("${api.security.token.refresh.expiration-time}")
            long refreshTokenExpiration,
            @Value("${api.security.token.refresh.prefix-blacklist}")
            String prefixBlacklist,
            RefreshTokenRepository refreshTokenRepository, UserRepository userRepository,
            RedisCacheService redisCacheService
    ) {
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.prefixBlacklist = prefixBlacklist;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.redisCacheService = redisCacheService;
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenExpiration);
        RefreshToken refreshToken = new RefreshToken(userId, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }


    @Transactional
    public Optional<RotateRefreshTokenDTO> rotateRefreshToken(String rawToken) {


        String tokenHash = hash(rawToken);

        return refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(RefreshToken::isValid)
                .flatMap(oldToken ->
                    userRepository.findById(oldToken.getUserId())
                            .filter(user ->
                                    user.getStatus() != UserStatus.BLOCKED &&
                                            user.getStatus() != UserStatus.DELETED)
                            .map(user -> {
                                        oldToken.revoke();
                                        String newToken = createRefreshToken(user.getId());
                                        return new RotateRefreshTokenDTO(newToken, user.getId());
                            })
                );
    }

    public void invalidateToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        String jti = decodedJWT.getId();
        Instant expiresAt = decodedJWT.getExpiresAtAsInstant();

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        redisCacheService.set(prefixBlacklist + jti, "revoked", ttl.getSeconds());
    }


    @Transactional
    public void revoke(String token) {
        String tokenHash = hash(token);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(RefreshToken::revoke);
    }


    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

}
