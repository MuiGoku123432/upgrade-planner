package com.sentinovo.carbuildervin.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OAuth 2.0 Dynamic Client Registration Response (RFC 7591).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 2.0 dynamic client registration response")
public class ClientRegistrationResponseDto {

    @JsonProperty("client_id")
    @Schema(description = "Unique client identifier", example = "dyn_abc123xyz")
    private String clientId;

    @JsonProperty("client_secret")
    @Schema(description = "Client secret (only for confidential clients)")
    private String clientSecret;

    @JsonProperty("client_id_issued_at")
    @Schema(description = "Timestamp when the client_id was issued")
    private Long clientIdIssuedAt;

    @JsonProperty("client_secret_expires_at")
    @Schema(description = "Timestamp when client_secret expires (0 = never)")
    private Long clientSecretExpiresAt;

    @JsonProperty("client_name")
    @Schema(description = "Human-readable name of the client")
    private String clientName;

    @JsonProperty("redirect_uris")
    @Schema(description = "Array of redirect URIs")
    private List<String> redirectUris;

    @JsonProperty("grant_types")
    @Schema(description = "Grant types the client can use")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    @Schema(description = "Response types the client can use")
    private List<String> responseTypes;

    @JsonProperty("token_endpoint_auth_method")
    @Schema(description = "Authentication method for token endpoint")
    private String tokenEndpointAuthMethod;

    @Schema(description = "Scopes the client can request")
    private String scope;
}
