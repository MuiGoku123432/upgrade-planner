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
 * OAuth 2.0 Authorization entity representing a user's grant of access
 * to an OAuth client for specific scopes.
 */
@Entity
@Table(name = "oauth_authorization",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "client_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthAuthorization that = (OAuthAuthorization) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OAuthAuthorization{id=" + id + ", userId=" +
               (user != null ? user.getId() : null) + ", clientId=" +
               (client != null ? client.getId() : null) + "}";
    }
}
