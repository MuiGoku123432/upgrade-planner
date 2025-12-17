package com.sentinovo.carbuildervin.service.oauth;

import com.sentinovo.carbuildervin.dto.oauth.*;
import com.sentinovo.carbuildervin.entities.oauth.*;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.exception.OAuthException;
import com.sentinovo.carbuildervin.repository.oauth.*;
import com.sentinovo.carbuildervin.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * OAuth 2.0 Authorization Server service.
 * Handles authorization requests, token exchange, and token refresh.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final OAuthClientRepository clientRepository;
    private final OAuthAuthorizationRepository authorizationRepository;
    private final OAuthAuthorizationCodeRepository authorizationCodeRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    private static final Map<String, String> SCOPE_DESCRIPTIONS = Map.of(
            "mcp:read", "Read your vehicles, builds, and parts",
            "mcp:write", "Create, update, and delete your data"
    );

    /**
     * Validate an authorization request.
     * Returns the client if valid, throws OAuthException if invalid.
     */
    public OAuthClient validateAuthorizationRequest(AuthorizationRequestDto request) {
        if (!"code".equals(request.getResponseType())) {
            throw new OAuthException(OAuthErrorDto.UNSUPPORTED_RESPONSE_TYPE,
                    "Only 'code' response_type is supported");
        }

        OAuthClient client = clientRepository.findByClientIdAndActive(request.getClientId())
                .orElseThrow(() -> new OAuthException(OAuthErrorDto.INVALID_CLIENT,
                        "Unknown client_id"));

        if (!isRedirectUriValid(client, request.getRedirectUri())) {
            throw new OAuthException(OAuthErrorDto.INVALID_REQUEST,
                    "Invalid redirect_uri");
        }

        String requestedScopes = request.getScope() != null ? request.getScope() : client.getScopes();
        if (!areScopesValid(client, requestedScopes)) {
            throw new OAuthException(OAuthErrorDto.INVALID_SCOPE,
                    "Requested scopes exceed client permissions");
        }

        return client;
    }

    /**
     * Build consent request data for display.
     */
    public ConsentRequestDto buildConsentRequest(OAuthClient client, AuthorizationRequestDto authRequest) {
        String scopes = authRequest.getScope() != null ? authRequest.getScope() : client.getScopes();
        List<String> scopeList = Arrays.asList(scopes.split("\\s+"));

        List<ConsentRequestDto.ScopeDescription> scopeDescriptions = scopeList.stream()
                .map(scope -> ConsentRequestDto.ScopeDescription.builder()
                        .scope(scope)
                        .description(SCOPE_DESCRIPTIONS.getOrDefault(scope, scope))
                        .build())
                .toList();

        return ConsentRequestDto.builder()
                .clientName(client.getClientName())
                .clientId(client.getClientId())
                .requestedScopes(scopeList)
                .scopeDescriptions(scopeDescriptions)
                .redirectUri(authRequest.getRedirectUri())
                .state(authRequest.getState())
                .scope(scopes)
                .build();
    }

    /**
     * Check if user has already authorized this client.
     */
    public boolean hasExistingAuthorization(UUID userId, UUID clientId) {
        return authorizationRepository.existsByUserIdAndClientId(userId, clientId);
    }

    /**
     * Create an authorization code for the user's grant.
     * Returns the plain code (to be sent to client).
     */
    public String createAuthorizationCode(User user, OAuthClient client, String scopes, String redirectUri) {
        // Create or update authorization
        OAuthAuthorization authorization = authorizationRepository
                .findByUserIdAndClientId(user.getId(), client.getId())
                .orElseGet(() -> OAuthAuthorization.builder()
                        .user(user)
                        .client(client)
                        .build());
        authorization.setScopes(scopes);
        authorizationRepository.save(authorization);

        // Generate authorization code
        String code = jwtTokenService.generateAuthorizationCode();
        String codeHash = jwtTokenService.hashToken(code);

        OAuthAuthorizationCode authCode = OAuthAuthorizationCode.builder()
                .codeHash(codeHash)
                .user(user)
                .client(client)
                .redirectUri(redirectUri)
                .scopes(scopes)
                .expiresAt(OffsetDateTime.now().plusSeconds(
                        jwtTokenService.getAuthorizationCodeExpirySeconds()))
                .used(false)
                .build();
        authorizationCodeRepository.save(authCode);

        log.info("Created authorization code for user {} and client {}", user.getId(), client.getClientId());
        return code;
    }

    /**
     * Exchange an authorization code for tokens.
     */
    public TokenResponseDto exchangeCodeForTokens(TokenRequestDto request) {
        // Validate client credentials
        OAuthClient client = validateClientCredentials(request.getClientId(), request.getClientSecret());

        // Find and validate authorization code
        String codeHash = jwtTokenService.hashToken(request.getCode());
        OAuthAuthorizationCode authCode = authorizationCodeRepository
                .findByCodeHashWithUserAndClient(codeHash)
                .orElseThrow(() -> new OAuthException(OAuthErrorDto.INVALID_GRANT,
                        "Invalid authorization code"));

        if (authCode.getUsed()) {
            log.warn("Authorization code reuse attempted for user {}", authCode.getUser().getId());
            // Revoke all tokens for this grant (security measure)
            refreshTokenRepository.revokeByUserIdAndClientId(
                    authCode.getUser().getId(), authCode.getClient().getId());
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "Authorization code has already been used");
        }

        if (authCode.isExpired()) {
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "Authorization code has expired");
        }

        if (!authCode.getClient().getId().equals(client.getId())) {
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "Authorization code was not issued to this client");
        }

        if (!authCode.getRedirectUri().equals(request.getRedirectUri())) {
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "redirect_uri does not match");
        }

        // Mark code as used
        authCode.setUsed(true);
        authorizationCodeRepository.save(authCode);

        // Generate tokens
        User user = authCode.getUser();
        String scopes = authCode.getScopes();

        String accessToken = jwtTokenService.generateAccessToken(user, scopes, client.getClientId());
        String refreshToken = createRefreshToken(user, client, scopes);

        log.info("Issued tokens for user {} via client {}", user.getId(), client.getClientId());

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessTokenExpirySeconds())
                .refreshToken(refreshToken)
                .scope(scopes)
                .build();
    }

    /**
     * Refresh an access token using a refresh token.
     */
    public TokenResponseDto refreshAccessToken(TokenRequestDto request) {
        // Validate client credentials
        OAuthClient client = validateClientCredentials(request.getClientId(), request.getClientSecret());

        // Find and validate refresh token
        String tokenHash = jwtTokenService.hashToken(request.getRefreshToken());
        OAuthRefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashWithUserAndClient(tokenHash)
                .orElseThrow(() -> new OAuthException(OAuthErrorDto.INVALID_GRANT,
                        "Invalid refresh token"));

        if (refreshToken.getIsRevoked()) {
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "Refresh token has expired");
        }

        if (!refreshToken.getClient().getId().equals(client.getId())) {
            throw new OAuthException(OAuthErrorDto.INVALID_GRANT,
                    "Refresh token was not issued to this client");
        }

        // Determine scopes (use requested or original)
        String scopes = request.getScope() != null ? request.getScope() : refreshToken.getScopes();
        if (!areScopesSubset(scopes, refreshToken.getScopes())) {
            throw new OAuthException(OAuthErrorDto.INVALID_SCOPE,
                    "Requested scopes exceed original grant");
        }

        User user = refreshToken.getUser();

        // Rotate refresh token (revoke old, create new)
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtTokenService.generateAccessToken(user, scopes, client.getClientId());
        String newRefreshToken = createRefreshToken(user, client, scopes);

        log.info("Refreshed tokens for user {} via client {}", user.getId(), client.getClientId());

        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessTokenExpirySeconds())
                .refreshToken(newRefreshToken)
                .scope(scopes)
                .build();
    }

    /**
     * Revoke all tokens for a user-client pair.
     */
    public void revokeClientAuthorization(UUID userId, UUID clientId) {
        refreshTokenRepository.revokeByUserIdAndClientId(userId, clientId);
        authorizationRepository.deleteByUserIdAndClientId(userId, clientId);
        log.info("Revoked authorization for user {} and client {}", userId, clientId);
    }

    /**
     * Get all authorized clients for a user.
     */
    @Transactional(readOnly = true)
    public List<OAuthAuthorization> getAuthorizedClients(UUID userId) {
        return authorizationRepository.findByUserIdWithClient(userId);
    }

    /**
     * Validate a bearer token and return the user if valid.
     */
    @Transactional(readOnly = true)
    public Optional<User> validateBearerToken(String token) {
        return jwtTokenService.validateAccessToken(token)
                .flatMap(claims -> jwtTokenService.extractUserId(claims))
                .flatMap(userId -> {
                    try {
                        return userRepository.findById(UUID.fromString(userId));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                })
                .filter(User::getIsActive);
    }

    /**
     * Clean up expired authorization codes and tokens.
     */
    public void cleanupExpiredTokens() {
        OffsetDateTime now = OffsetDateTime.now();
        int codesDeleted = authorizationCodeRepository.deleteExpired(now);
        int tokensDeleted = refreshTokenRepository.deleteExpired(now);
        log.info("Cleaned up {} expired authorization codes and {} expired refresh tokens",
                codesDeleted, tokensDeleted);
    }

    /**
     * Register a new OAuth client dynamically (RFC 7591).
     * Used by MCP clients like ChatGPT Desktop to register themselves.
     */
    public ClientRegistrationResponseDto registerClient(ClientRegistrationRequestDto request) {
        // Validate required fields
        if (request.getRedirectUris() == null || request.getRedirectUris().isEmpty()) {
            throw new OAuthException(OAuthErrorDto.INVALID_REQUEST,
                    "redirect_uris is required");
        }

        // Generate unique client_id
        String clientId = "dyn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // Determine if client is confidential or public based on token_endpoint_auth_method
        String authMethod = request.getTokenEndpointAuthMethod();
        boolean isConfidential = !"none".equals(authMethod);

        // Generate client secret for confidential clients
        String clientSecret = null;
        String hashedSecret = null;
        if (isConfidential) {
            clientSecret = jwtTokenService.generateRefreshToken(); // Reuse secure random generation
            hashedSecret = passwordEncoder.encode(clientSecret);
        } else {
            // Public clients still need a placeholder secret (database NOT NULL constraint)
            hashedSecret = passwordEncoder.encode(UUID.randomUUID().toString());
        }

        // Determine client name (use provided or generate default)
        String clientName = request.getClientName();
        if (clientName == null || clientName.isBlank()) {
            clientName = "Dynamic Client " + clientId.substring(4, 12);
        }

        // Build redirect URIs as JSON array string
        String redirectUris = "[" + String.join(",",
                request.getRedirectUris().stream()
                        .map(uri -> "\"" + uri + "\"")
                        .toList()) + "]";

        // Determine scopes (use provided or default)
        String scopes = request.getScope();
        if (scopes == null || scopes.isBlank()) {
            scopes = "mcp:read mcp:write";
        }

        // Create and save the client
        OAuthClient client = OAuthClient.builder()
                .clientId(clientId)
                .clientSecret(hashedSecret)
                .clientName(clientName)
                .redirectUris(redirectUris)
                .scopes(scopes)
                .isConfidential(isConfidential)
                .isActive(true)
                .build();

        clientRepository.save(client);
        log.info("Registered new OAuth client: {} ({})", clientId, clientName);

        // Build response
        List<String> grantTypes = request.getGrantTypes();
        if (grantTypes == null || grantTypes.isEmpty()) {
            grantTypes = List.of("authorization_code", "refresh_token");
        }

        List<String> responseTypes = request.getResponseTypes();
        if (responseTypes == null || responseTypes.isEmpty()) {
            responseTypes = List.of("code");
        }

        return ClientRegistrationResponseDto.builder()
                .clientId(clientId)
                .clientSecret(isConfidential ? clientSecret : "")
                .clientIdIssuedAt(System.currentTimeMillis() / 1000)
                .clientSecretExpiresAt(0L) // Never expires
                .clientName(clientName)
                .redirectUris(request.getRedirectUris())
                .grantTypes(grantTypes)
                .responseTypes(responseTypes)
                .tokenEndpointAuthMethod(isConfidential ? "client_secret_post" : "none")
                .scope(scopes)
                .build();
    }

    // --- Private helpers ---

    private OAuthClient validateClientCredentials(String clientId, String clientSecret) {
        OAuthClient client = clientRepository.findByClientIdAndActive(clientId)
                .orElseThrow(() -> new OAuthException(OAuthErrorDto.INVALID_CLIENT,
                        "Invalid client credentials"));

        if (client.getIsConfidential() && !passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            throw new OAuthException(OAuthErrorDto.INVALID_CLIENT,
                    "Invalid client credentials");
        }

        return client;
    }

    private String createRefreshToken(User user, OAuthClient client, String scopes) {
        String token = jwtTokenService.generateRefreshToken();
        String tokenHash = jwtTokenService.hashToken(token);

        OAuthRefreshToken refreshToken = OAuthRefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .client(client)
                .scopes(scopes)
                .expiresAt(OffsetDateTime.now().plusSeconds(
                        jwtTokenService.getRefreshTokenExpirySeconds()))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    private boolean isRedirectUriValid(OAuthClient client, String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return false;
        }

        // Parse stored redirect URIs (simple comma or JSON array)
        String storedUris = client.getRedirectUris();

        // Handle JSON array format
        if (storedUris.startsWith("[")) {
            storedUris = storedUris.substring(1, storedUris.length() - 1)
                    .replace("\"", "")
                    .replace("'", "");
        }

        for (String uri : storedUris.split(",")) {
            String trimmedUri = uri.trim();

            // Handle wildcard patterns (e.g., http://localhost:*)
            if (trimmedUri.contains("*")) {
                String pattern = trimmedUri.replace("*", ".*");
                if (redirectUri.matches(pattern)) {
                    return true;
                }
            } else if (trimmedUri.equals(redirectUri)) {
                return true;
            }
        }

        return false;
    }

    private boolean areScopesValid(OAuthClient client, String requestedScopes) {
        if (requestedScopes == null || requestedScopes.isBlank()) {
            return true;
        }
        return areScopesSubset(requestedScopes, client.getScopes());
    }

    private boolean areScopesSubset(String requested, String allowed) {
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowed.split("\\s+")));
        for (String scope : requested.split("\\s+")) {
            if (!allowedSet.contains(scope)) {
                return false;
            }
        }
        return true;
    }
}
