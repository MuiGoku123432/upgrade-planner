package com.sentinovo.carbuildervin.entities.oauth;

import com.sentinovo.carbuildervin.entities.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * OAuth 2.0 Refresh Token entity representing a long-lived token
 * that can be used to obtain new access tokens.
 * The token value itself is stored as a SHA-256 hash.
 */
@Entity
@Table(name = "oauth_refresh_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Token hash is required")
    @Size(max = 64)
    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private OAuthClient client;

    @NotBlank(message = "Scopes are required")
    @Size(max = 255)
    @Column(name = "scopes", length = 255, nullable = false)
    private String scopes;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    public void revoke() {
        this.isRevoked = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthRefreshToken that = (OAuthRefreshToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OAuthRefreshToken{id=" + id + ", isRevoked=" + isRevoked +
               ", expiresAt=" + expiresAt + "}";
    }
}
