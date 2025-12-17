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
 * OAuth 2.0 Authorization Code entity representing a temporary code
 * that can be exchanged for access and refresh tokens.
 * Authorization codes are short-lived (typically 10 minutes).
 */
@Entity
@Table(name = "oauth_authorization_code")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorizationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Code hash is required")
    @Size(max = 64)
    @Column(name = "code_hash", length = 64, nullable = false, unique = true)
    private String codeHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private OAuthClient client;

    @NotBlank(message = "Redirect URI is required")
    @Size(max = 500)
    @Column(name = "redirect_uri", length = 500, nullable = false)
    private String redirectUri;

    @NotBlank(message = "Scopes are required")
    @Size(max = 255)
    @Column(name = "scopes", length = 255, nullable = false)
    private String scopes;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthAuthorizationCode that = (OAuthAuthorizationCode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OAuthAuthorizationCode{id=" + id + ", used=" + used +
               ", expiresAt=" + expiresAt + "}";
    }
}
