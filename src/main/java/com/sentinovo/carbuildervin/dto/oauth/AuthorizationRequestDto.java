package com.sentinovo.carbuildervin.dto.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 Authorization Request parameters.
 * Used when redirecting to /oauth/authorize endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 2.0 authorization request parameters")
public class AuthorizationRequestDto {

    @NotBlank(message = "response_type is required")
    @Schema(description = "Must be 'code' for authorization code flow", example = "code")
    private String responseType;

    @NotBlank(message = "client_id is required")
    @Schema(description = "The OAuth client identifier", example = "chatgpt-desktop")
    private String clientId;

    @NotBlank(message = "redirect_uri is required")
    @Schema(description = "The URI to redirect to after authorization", example = "https://chat.openai.com/callback")
    private String redirectUri;

    @Schema(description = "Space-separated list of requested scopes", example = "mcp:read mcp:write")
    private String scope;

    @Schema(description = "Client state value for CSRF protection", example = "xyz123")
    private String state;

    /**
     * Validates that this is a valid authorization code request.
     */
    public boolean isAuthorizationCodeRequest() {
        return "code".equals(responseType);
    }
}
