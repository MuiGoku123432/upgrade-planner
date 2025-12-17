package com.sentinovo.carbuildervin.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 Error Response.
 * Standard error format per RFC 6749.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 2.0 error response")
public class OAuthErrorDto {

    @Schema(description = "Error code", example = "invalid_grant")
    private String error;

    @JsonProperty("error_description")
    @Schema(description = "Human-readable error description",
            example = "The authorization code has expired")
    private String errorDescription;

    @JsonProperty("error_uri")
    @Schema(description = "URI with more information about the error")
    private String errorUri;

    /**
     * Standard OAuth 2.0 error codes.
     */
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_CLIENT = "invalid_client";
    public static final String INVALID_GRANT = "invalid_grant";
    public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String INVALID_SCOPE = "invalid_scope";
    public static final String ACCESS_DENIED = "access_denied";
    public static final String UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
    public static final String SERVER_ERROR = "server_error";
    public static final String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";

    public static OAuthErrorDto invalidRequest(String description) {
        return OAuthErrorDto.builder()
                .error(INVALID_REQUEST)
                .errorDescription(description)
                .build();
    }

    public static OAuthErrorDto invalidClient(String description) {
        return OAuthErrorDto.builder()
                .error(INVALID_CLIENT)
                .errorDescription(description)
                .build();
    }

    public static OAuthErrorDto invalidGrant(String description) {
        return OAuthErrorDto.builder()
                .error(INVALID_GRANT)
                .errorDescription(description)
                .build();
    }

    public static OAuthErrorDto accessDenied(String description) {
        return OAuthErrorDto.builder()
                .error(ACCESS_DENIED)
                .errorDescription(description)
                .build();
    }

    public static OAuthErrorDto unsupportedGrantType(String description) {
        return OAuthErrorDto.builder()
                .error(UNSUPPORTED_GRANT_TYPE)
                .errorDescription(description)
                .build();
    }

    public static OAuthErrorDto serverError(String description) {
        return OAuthErrorDto.builder()
                .error(SERVER_ERROR)
                .errorDescription(description)
                .build();
    }
}
