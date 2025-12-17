package com.sentinovo.carbuildervin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for OAuth 2.0 authentication.
 */
@Component
@ConfigurationProperties(prefix = "app.oauth")
public class OAuthProperties {

    /**
     * Secret key for signing JWT access tokens.
     * Should be at least 256 bits (32 characters) for HMAC-SHA256.
     */
    private String jwtSecret;

    /**
     * Access token expiry time in seconds.
     * Default: 3600 (1 hour)
     */
    private int accessTokenExpiry = 3600;

    /**
     * Refresh token expiry time in seconds.
     * Default: 2592000 (30 days)
     */
    private int refreshTokenExpiry = 2592000;

    /**
     * Authorization code expiry time in seconds.
     * Default: 600 (10 minutes)
     */
    private int authorizationCodeExpiry = 600;

    /**
     * Issuer claim for JWT tokens.
     * Should be the base URL of the server.
     */
    private String issuer = "http://localhost:8080";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public int getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public void setAccessTokenExpiry(int accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public int getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(int refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public int getAuthorizationCodeExpiry() {
        return authorizationCodeExpiry;
    }

    public void setAuthorizationCodeExpiry(int authorizationCodeExpiry) {
        this.authorizationCodeExpiry = authorizationCodeExpiry;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
