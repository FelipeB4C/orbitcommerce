package com.orbitcommerce.identity.controller;

import com.orbitcommerce.identity.dto.DataLoginDTO;
import com.orbitcommerce.identity.dto.DataTokenDTO;
import com.orbitcommerce.identity.dto.RefreshTokenDTO;
import com.orbitcommerce.identity.model.User;
import com.orbitcommerce.identity.repository.UserRepository;
import com.orbitcommerce.identity.service.RefreshTokenService;
import com.orbitcommerce.identity.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoint for authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @Value("${api.security.token.jwt.type}")
    private String tokenType;

    @Value("${api.security.token.jwt.expiration-time}")
    private Long expiresIn;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, TokenService tokenService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(summary = "Authenticate user and issue tokens", description = "Validates user credentials " +
            "(email and password), returning an RS256-signed JWT Access Token (15-minute lifespan) " +
            "and an opaque UUID Refresh Token (30-day lifespan)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication success"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account locked or deleted")
    })
    @PostMapping("/login")
    public ResponseEntity<DataTokenDTO> login(@RequestBody @Valid DataLoginDTO dataLoginDTO) {
        var authToken = new UsernamePasswordAuthenticationToken(dataLoginDTO.email(), dataLoginDTO.password());
        var auth = authenticationManager.authenticate(authToken);
        String tokenAccess = tokenService.generateToken((User) auth.getPrincipal());
        String refreshToken = refreshTokenService.createRefreshToken(((User) auth.getPrincipal()).getId());
        return ResponseEntity.ok(new DataTokenDTO(tokenType, tokenAccess, expiresIn, refreshToken));
    }

    @Operation(summary = "Revoke current user session", description = "Performs single-device session logout. " +
            "Adds the current access token's JTI to the Redis deny-list (blacklist:{jti}) " +
            "and marks the provided refresh token as revoked in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Token expired or invalid")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, @RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        String accessToken = tokenService.extractTokenFromRequest(request);
        refreshTokenService.invalidateToken(accessToken);
        refreshTokenService.revoke(refreshTokenDTO.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Refresh access token with token rotation", description = "Generates a new JWT Access Token " +
            "from a valid opaque Refresh Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New refresh token generated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Refresh token expired, revoked or invalid")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshTokenDTO refreshTokenDTO) {
        var rotation = refreshTokenService.rotateRefreshToken(refreshTokenDTO.refreshToken())
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalid, expired, or already used"));
        var user = userRepository.findById(rotation.userId()).orElseThrow(() -> new BadCredentialsException("User for refresh token not found"));

        String newAccessToken = tokenService.generateToken(user);
        return ResponseEntity.ok(new DataTokenDTO(tokenType, newAccessToken, expiresIn, rotation.newRefreshToken()));
    }

}
