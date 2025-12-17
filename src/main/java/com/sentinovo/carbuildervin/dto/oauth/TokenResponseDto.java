package com.sentinovo.carbuildervin.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 Token Response.
 * Returned from the /oauth/token endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 2.0 token response")
public class TokenResponseDto {

    @JsonProperty("access_token")
    @Schema(description = "The access token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @JsonProperty("token_type")
    @Schema(description = "The token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    @Schema(description = "Token expiry in seconds", example = "3600")
    private int expiresIn;

    @JsonProperty("refresh_token")
    @Schema(description = "The refresh token for obtaining new access tokens")
    private String refreshToken;

    @Schema(description = "The scopes granted", example = "mcp:read mcp:write")
    private String scope;
}
