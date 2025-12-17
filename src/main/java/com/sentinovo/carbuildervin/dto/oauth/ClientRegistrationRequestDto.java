package com.sentinovo.carbuildervin.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OAuth 2.0 Dynamic Client Registration Request (RFC 7591).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 2.0 dynamic client registration request")
public class ClientRegistrationRequestDto {

    @JsonProperty("client_name")
    @Schema(description = "Human-readable name of the client", example = "ChatGPT Desktop")
    private String clientName;

    @JsonProperty("redirect_uris")
    @Schema(description = "Array of redirect URIs", example = "[\"http://localhost:8080/callback\"]")
    private List<String> redirectUris;

    @JsonProperty("grant_types")
    @Schema(description = "Grant types the client will use", example = "[\"authorization_code\", \"refresh_token\"]")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    @Schema(description = "Response types the client will use", example = "[\"code\"]")
    private List<String> responseTypes;

    @JsonProperty("token_endpoint_auth_method")
    @Schema(description = "Authentication method for token endpoint", example = "none")
    private String tokenEndpointAuthMethod;

    @Schema(description = "Requested scopes", example = "mcp:read mcp:write")
    private String scope;

    @JsonProperty("client_uri")
    @Schema(description = "URL of the client's home page")
    private String clientUri;

    @JsonProperty("logo_uri")
    @Schema(description = "URL of the client's logo")
    private String logoUri;
}
