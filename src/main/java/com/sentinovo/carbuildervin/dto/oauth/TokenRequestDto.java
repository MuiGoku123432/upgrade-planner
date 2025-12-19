package com.sentinovo.carbuildervin.dto.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 Token Request.
 * Used when exchanging authorization code for tokens or refreshing tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 2.0 token request")
public class TokenRequestDto {

    @NotBlank(message = "grant_type is required")
    @Schema(description = "The grant type: 'authorization_code' or 'refresh_token'",
            example = "authorization_code")
    private String grantType;

    @Schema(description = "The authorization code (for authorization_code grant)",
            example = "auth_code_abc123")
    private String code;

    @Schema(description = "The redirect URI used in the authorization request",
            example = "https://chat.openai.com/callback")
    private String redirectUri;

    @Schema(description = "The OAuth client identifier", example = "chatgpt-desktop")
    private String clientId;

    @Schema(description = "The OAuth client secret")
    private String clientSecret;

    @Schema(description = "The refresh token (for refresh_token grant)")
    private String refreshToken;

    @Schema(description = "Optional scope for refresh_token grant")
    private String scope;

    @Schema(description = "PKCE code verifier (for authorization_code grant with PKCE)")
    private String codeVerifier;

    public boolean isAuthorizationCodeGrant() {
        return "authorization_code".equals(grantType);
    }

    public boolean isRefreshTokenGrant() {
        return "refresh_token".equals(grantType);
    }
}
