package com.orbitcommerce.identity.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.orbitcommerce.identity.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;


@Service
public class TokenService {

    @Value("${api.security.token.jwt.issuer}")
    private String issuer;

    @Value("${api.security.token.jwt.expiration-time}")
    private long expirationTimeValue;

    @Value("${api.security.token.refresh.prefix-blacklist}")
    private String prefixBlacklist;

    private RedisCacheService redisCacheService;

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    // Inicializa o algoritmo e o verifier apenas UMA vez ao subir o serviço (ganho de performance)
    public TokenService(
            RedisCacheService redisCacheService,
            @Value("${api.security.token.jwt.issuer}")
            String issuer,
            @Value("${api.security.token.jwt.expiration-time}")
            long expirationTimeValue,
            @Value("${api.security.token.refresh.prefix-blacklist}")
            String prefixBlacklist,
            @Value("${api.security.token.jwt.public-key-path}")
            Resource publicKeyResource,
            @Value("${api.security.token.jwt.private-key-path}")
            Resource privateKeyResource) {

        this.redisCacheService = redisCacheService;
        this.issuer = issuer;
        this.expirationTimeValue = expirationTimeValue;
        this.prefixBlacklist = prefixBlacklist;

        KeyPair keyPair = generateKeyPair();

        RSAPublicKey publicKey = loadPublicKey(publicKeyResource);
        RSAPrivateKey privateKey = loadPrivateKey(privateKeyResource);

        // Inicializa o algoritmo com o par gerado em memória
        this.algorithm = Algorithm.RSA256(publicKey, privateKey);

        // Constrói o verificador reutilizável
        this.verifier = JWT.require(this.algorithm)
                .withIssuer(this.issuer)
                .build();
    }

    public String generateToken(User user) {
        try {
            String jti = UUID.randomUUID().toString();

            return JWT.create()
                    .withIssuer(this.issuer)
                    .withSubject(user.getEmail())// Ou user.getUsername()
                    .withClaim("uid", user.getId().toString())
                    .withJWTId(jti)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plusSeconds(expirationTimeValue))
                    .sign(this.algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT de acesso", exception);
        }
    }


    public String tokenVerify(String token) {

        try {
            DecodedJWT decodedJWT = this.verifier.verify(token);
            String jti = decodedJWT.getId();
            String userId = decodedJWT.getClaim("uid").asString();
            Instant issuedAt = decodedJWT.getIssuedAtAsInstant();

            if (isBlacklisted(jti)) return null;

            if (isRevokedByStamp(userId, issuedAt)) return null;

            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception) {
            // Altere APENAS este catch para expor o motivo real do erro 403:
            System.err.println("ERRO NA VALIDAÇÃO DO JWT: " + exception.getMessage());
            return null;
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar chaves RSA", e);
        }
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

    private boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisCacheService.hasKey(prefixBlacklist + jti));
    }

    private RSAPublicKey loadPublicKey(Resource key) {
        try {
            String cleanKey = readResourceContent(key)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(cleanKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao carregar chave pública RSA", e);
        }
    }

    private boolean isRevokedByStamp(String userId, Instant issuedAt) {
        String stamp = redisCacheService.get(prefixBlacklist + userId);
        if (stamp == null) return false;
        return !issuedAt.isAfter(Instant.parse(stamp)); // token emitido antes do carimbo -> morto
    }

    /**
     * Derruba TODOS os access tokens do usuário, em qualquer dispositivo, de uma vez.
     */
    public void revokeAllSessions(UUID userId) {
        redisCacheService.set(
                prefixBlacklist + userId.toString(),
                Instant.now().toString(),
                expirationTimeValue // TTL = vida máxima de um access token (15min)
        );
    }

    private RSAPrivateKey loadPrivateKey(Resource key) {
        try {
            String cleanKey = readResourceContent(key)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(cleanKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao carregar chave privada RSA", e);
        }
    }

    private String readResourceContent(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
