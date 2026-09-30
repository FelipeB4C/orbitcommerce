package com.orbitcommerce.identity.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_user", columnList = "user_id")
        })
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id", nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshToken(UUID userid, String tokenHash, Instant expiresAt) {
        this.userId = userid;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
    }

    public boolean isValid() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
    }

}
