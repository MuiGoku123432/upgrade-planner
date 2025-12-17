package com.sentinovo.carbuildervin.service.oauth;

import com.sentinovo.carbuildervin.config.OAuthProperties;
import com.sentinovo.carbuildervin.entities.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Service for JWT access token generation and validation,
 * as well as random token generation for refresh tokens and authorization codes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    private final OAuthProperties oAuthProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String CLAIM_USER_ID = "sub";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_CLIENT_ID = "client_id";
    private static final String CLAIM_SCOPE = "scope";

    /**
     * Generate a JWT access token for a user.
     */
    public String generateAccessToken(User user, String scopes, String clientId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(oAuthProperties.getAccessTokenExpiry());

        return Jwts.builder()
                .issuer(oAuthProperties.getIssuer())
                .subject(user.getId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_CLIENT_ID, clientId)
                .claim(CLAIM_SCOPE, scopes)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate a JWT access token and extract claims.
     */
    public Optional<Claims> validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(oAuthProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract user ID from JWT claims.
     */
    public Optional<String> extractUserId(Claims claims) {
        return Optional.ofNullable(claims.getSubject());
    }

    /**
     * Extract username from JWT claims.
     */
    public Optional<String> extractUsername(Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_USERNAME, String.class));
    }

    /**
     * Extract client ID from JWT claims.
     */
    public Optional<String> extractClientId(Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_CLIENT_ID, String.class));
    }

    /**
     * Extract scopes from JWT claims.
     */
    public Optional<String> extractScopes(Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_SCOPE, String.class));
    }

    /**
     * Generate a random refresh token.
     * Returns the plain token (store the hash in the database).
     */
    public String generateRefreshToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Generate a random authorization code.
     * Returns the plain code (store the hash in the database).
     */
    public String generateAuthorizationCode() {
        byte[] codeBytes = new byte[32];
        secureRandom.nextBytes(codeBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeBytes);
    }

    /**
     * Hash a token using SHA-256.
     * Used to securely store refresh tokens and authorization codes.
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Get access token expiry in seconds.
     */
    public int getAccessTokenExpirySeconds() {
        return oAuthProperties.getAccessTokenExpiry();
    }

    /**
     * Get refresh token expiry in seconds.
     */
    public int getRefreshTokenExpirySeconds() {
        return oAuthProperties.getRefreshTokenExpiry();
    }

    /**
     * Get authorization code expiry in seconds.
     */
    public int getAuthorizationCodeExpirySeconds() {
        return oAuthProperties.getAuthorizationCodeExpiry();
    }

    private SecretKey getSigningKey() {
        String secret = oAuthProperties.getJwtSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "OAuth JWT secret must be at least 32 characters. " +
                    "Set app.oauth.jwt-secret in application.properties or OAUTH_JWT_SECRET env var.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
