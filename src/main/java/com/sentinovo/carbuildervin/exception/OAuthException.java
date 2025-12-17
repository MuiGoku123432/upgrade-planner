package com.sentinovo.carbuildervin.exception;

import com.sentinovo.carbuildervin.dto.oauth.OAuthErrorDto;

/**
 * Exception for OAuth 2.0 errors.
 * Contains the standard OAuth error code and description.
 */
public class OAuthException extends BusinessException {

    private final String oauthError;
    private final String errorDescription;

    public OAuthException(String oauthError, String errorDescription) {
        super("OAUTH_ERROR", String.format("%s: %s", oauthError, errorDescription));
        this.oauthError = oauthError;
        this.errorDescription = errorDescription;
    }

    public String getOauthError() {
        return oauthError;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public OAuthErrorDto toErrorDto() {
        return OAuthErrorDto.builder()
                .error(oauthError)
                .errorDescription(errorDescription)
                .build();
    }
}
