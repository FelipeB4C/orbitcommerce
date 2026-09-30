package com.orbitcommerce.identity.model;

import com.orbitcommerce.identity.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        })
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    @Size(min = 1, max = 150)
    @NotBlank
    private String fullName;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Email
    @Size(min = 1, max = 180)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name= "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public User(String fullName, String email, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BLOCKED;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void anonymizeAndDelete() {
        this.email = "deleted-" + this.id + "@anon.orbitcommerce.local"; // libera o email original
        this.fullName = "User Removed";
        this.status = UserStatus.DELETED;
        this.deletedAt = Instant.now();
    }

}
