package com.sentinovo.carbuildervin.controller.oauth;

import com.sentinovo.carbuildervin.config.OAuthProperties;
import com.sentinovo.carbuildervin.dto.oauth.*;
import com.sentinovo.carbuildervin.entities.oauth.OAuthClient;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.exception.OAuthException;
import com.sentinovo.carbuildervin.service.oauth.OAuthService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth 2.0 Authorization Server controller.
 * Handles authorization requests, consent, and token endpoints.
 */
@Controller
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth", description = "OAuth 2.0 Authorization endpoints")
public class OAuthController {

    private final OAuthService oAuthService;
    private final AuthenticationService authenticationService;
    private final OAuthProperties oAuthProperties;

    private static final String SESSION_OAUTH_REQUEST = "oauth_auth_request";

    /**
     * Authorization endpoint - initiates the OAuth flow.
     * If user is not logged in, redirects to login.
     * If user hasn't consented, shows consent page.
     * If user has already consented, redirects with code.
     */
    @GetMapping("/authorize")
    @Operation(summary = "OAuth authorization endpoint", description = "Initiates OAuth 2.0 authorization code flow")
    public String authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
            HttpSession session,
            Model model) {

        AuthorizationRequestDto authRequest = AuthorizationRequestDto.builder()
                .responseType(responseType)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .scope(scope)
                .state(state)
                .codeChallenge(codeChallenge)
                .codeChallengeMethod(codeChallengeMethod)
                .build();

        try {
            // Validate the request and get the client
            OAuthClient client = oAuthService.validateAuthorizationRequest(authRequest);

            // Check if user is authenticated
            Optional<User> currentUser = authenticationService.getCurrentUser();
            if (currentUser.isEmpty()) {
                // Store OAuth request in session and redirect to login
                session.setAttribute(SESSION_OAUTH_REQUEST, authRequest);
                return "redirect:/login?oauth=true";
            }

            User user = currentUser.get();

            // Check if user has already authorized this client
            if (oAuthService.hasExistingAuthorization(user.getId(), client.getId())) {
                // Skip consent, issue code directly
                String scopes = scope != null ? scope : client.getScopes();
                String code = oAuthService.createAuthorizationCode(user, client, scopes, redirectUri,
                        codeChallenge, codeChallengeMethod);
                return buildRedirectWithCode(redirectUri, code, state);
            }

            // Show consent page
            ConsentRequestDto consentRequest = oAuthService.buildConsentRequest(client, authRequest);
            model.addAttribute("consent", consentRequest);
            session.setAttribute(SESSION_OAUTH_REQUEST, authRequest);
            return "oauth/consent";

        } catch (OAuthException e) {
            log.warn("OAuth authorization error: {}", e.getMessage());
            return buildErrorRedirect(redirectUri, e.getOauthError(), e.getErrorDescription(), state);
        }
    }

    /**
     * Continue authorization after login.
     * Called when user logs in during OAuth flow.
     */
    @GetMapping("/authorize/continue")
    public String continueAuthorization(HttpSession session, Model model) {
        AuthorizationRequestDto authRequest = (AuthorizationRequestDto) session.getAttribute(SESSION_OAUTH_REQUEST);
        if (authRequest == null) {
            return "redirect:/";
        }

        return authorize(
                authRequest.getResponseType(),
                authRequest.getClientId(),
                authRequest.getRedirectUri(),
                authRequest.getScope(),
                authRequest.getState(),
                authRequest.getCodeChallenge(),
                authRequest.getCodeChallengeMethod(),
                session,
                model
        );
    }

    /**
     * Process user consent decision.
     */
    @PostMapping("/authorize")
    public String processConsent(
            @RequestParam("decision") String decision,
            HttpSession session) {

        AuthorizationRequestDto authRequest = (AuthorizationRequestDto) session.getAttribute(SESSION_OAUTH_REQUEST);
        session.removeAttribute(SESSION_OAUTH_REQUEST);

        if (authRequest == null) {
            return "redirect:/";
        }

        String redirectUri = authRequest.getRedirectUri();
        String state = authRequest.getState();

        if (!"approve".equals(decision)) {
            return buildErrorRedirect(redirectUri, OAuthErrorDto.ACCESS_DENIED,
                    "User denied access", state);
        }

        try {
            OAuthClient client = oAuthService.validateAuthorizationRequest(authRequest);
            User user = authenticationService.getCurrentUserOrThrow();

            String scopes = authRequest.getScope() != null ? authRequest.getScope() : client.getScopes();
            String code = oAuthService.createAuthorizationCode(user, client, scopes, redirectUri,
                    authRequest.getCodeChallenge(), authRequest.getCodeChallengeMethod());

            log.info("User {} approved OAuth authorization for client {}", user.getId(), client.getClientId());
            return buildRedirectWithCode(redirectUri, code, state);

        } catch (OAuthException e) {
            log.warn("OAuth consent processing error: {}", e.getMessage());
            return buildErrorRedirect(redirectUri, e.getOauthError(), e.getErrorDescription(), state);
        }
    }

    /**
     * Token endpoint - exchanges authorization code for tokens or refreshes tokens.
     * This is a REST endpoint that returns JSON.
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "OAuth token endpoint", description = "Exchange authorization code for tokens or refresh tokens")
    public ResponseEntity<?> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier) {

        TokenRequestDto tokenRequest = TokenRequestDto.builder()
                .grantType(grantType)
                .code(code)
                .redirectUri(redirectUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .refreshToken(refreshToken)
                .scope(scope)
                .codeVerifier(codeVerifier)
                .build();

        try {
            TokenResponseDto response;

            if (tokenRequest.isAuthorizationCodeGrant()) {
                response = oAuthService.exchangeCodeForTokens(tokenRequest);
            } else if (tokenRequest.isRefreshTokenGrant()) {
                response = oAuthService.refreshAccessToken(tokenRequest);
            } else {
                throw new OAuthException(OAuthErrorDto.UNSUPPORTED_GRANT_TYPE,
                        "Grant type '" + grantType + "' is not supported");
            }

            return ResponseEntity.ok(response);

        } catch (OAuthException e) {
            log.warn("OAuth token error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.toErrorDto());
        }
    }

    /**
     * Token revocation endpoint.
     */
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    @Operation(summary = "OAuth token revocation", description = "Revoke a refresh token")
    public ResponseEntity<?> revoke(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint) {

        // For now, we just return success - tokens will expire naturally
        // A full implementation would look up and revoke the token
        log.info("Token revocation requested");
        return ResponseEntity.ok().build();
    }

    /**
     * Dynamic Client Registration endpoint (RFC 7591).
     * Allows MCP clients to register themselves automatically.
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "OAuth client registration", description = "Register a new OAuth client dynamically (RFC 7591)")
    public ResponseEntity<?> registerClient(@RequestBody ClientRegistrationRequestDto request) {
        try {
            ClientRegistrationResponseDto response = oAuthService.registerClient(request);
            log.info("Dynamically registered OAuth client: {}", response.getClientId());
            return ResponseEntity.status(201).body(response);
        } catch (OAuthException e) {
            log.warn("Client registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.toErrorDto());
        }
    }

    // --- Private helpers ---

    private String buildRedirectWithCode(String redirectUri, String code, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code);
        if (state != null) {
            builder.queryParam("state", state);
        }
        return "redirect:" + builder.toUriString();
    }

    private String buildErrorRedirect(String redirectUri, String error, String description, String state) {
        if (redirectUri == null || redirectUri.isBlank()) {
            // Can't redirect, show error page
            return "redirect:/oauth/error?error=" + encode(error) + "&error_description=" + encode(description);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", error)
                .queryParam("error_description", description);
        if (state != null) {
            builder.queryParam("state", state);
        }
        return "redirect:" + builder.toUriString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
