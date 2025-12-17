package com.sentinovo.carbuildervin.entities.oauth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * OAuth 2.0 Client entity representing registered applications
 * (e.g., ChatGPT Desktop) that can request access to user data.
 */
@Entity
@Table(name = "oauth_client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Client ID is required")
    @Size(max = 100)
    @Column(name = "client_id", length = 100, nullable = false, unique = true)
    private String clientId;

    @NotBlank(message = "Client secret is required")
    @Size(max = 255)
    @Column(name = "client_secret", length = 255, nullable = false)
    private String clientSecret;

    @NotBlank(message = "Client name is required")
    @Size(max = 100)
    @Column(name = "client_name", length = 100, nullable = false)
    private String clientName;

    @NotBlank(message = "Redirect URIs are required")
    @Column(name = "redirect_uris", columnDefinition = "TEXT", nullable = false)
    private String redirectUris;

    @Size(max = 255)
    @Column(name = "scopes", length = 255)
    @Builder.Default
    private String scopes = "mcp:read mcp:write";

    @Column(name = "is_confidential", nullable = false)
    @Builder.Default
    private Boolean isConfidential = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthClient that = (OAuthClient) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OAuthClient{id=" + id + ", clientId='" + clientId + "', clientName='" + clientName + "'}";
    }
}
