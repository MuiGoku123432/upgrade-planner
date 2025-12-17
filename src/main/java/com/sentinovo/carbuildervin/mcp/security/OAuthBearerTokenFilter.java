package com.sentinovo.carbuildervin.mcp.security;

import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.service.oauth.JwtTokenService;
import com.sentinovo.carbuildervin.service.oauth.OAuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that authenticates requests using OAuth 2.0 Bearer tokens.
 * Processes requests to /mcp/** endpoints that have an Authorization header.
 *
 * This filter runs before the MCP API key filter, so Bearer tokens take precedence.
 * If no Bearer token is present, the request continues to the API key filter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // Run before API key filter
public class OAuthBearerTokenFilter extends OncePerRequestFilter {

    private final OAuthService oAuthService;
    private final JwtTokenService jwtTokenService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Only process MCP endpoints
        if (!requestPath.startsWith("/mcp")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check for Authorization header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // No Bearer token - let the API key filter handle it
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            log.debug("Empty Bearer token for request: {}", requestPath);
            // Continue to API key filter
            filterChain.doFilter(request, response);
            return;
        }

        // Validate the JWT token
        Optional<Claims> claimsOpt = jwtTokenService.validateAccessToken(token);

        if (claimsOpt.isEmpty()) {
            log.debug("Invalid or expired Bearer token for request: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\",\"error_description\":\"The access token is invalid or has expired\"}");
            return;
        }

        Claims claims = claimsOpt.get();

        // Look up user
        Optional<User> userOpt = oAuthService.validateBearerToken(token);

        if (userOpt.isEmpty()) {
            log.warn("Bearer token valid but user not found or inactive for request: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\",\"error_description\":\"User not found or inactive\"}");
            return;
        }

        User user = userOpt.get();
        String scopes = jwtTokenService.extractScopes(claims).orElse("");

        log.debug("OAuth Bearer token authenticated for user: {} with scopes: {}", user.getUsername(), scopes);

        // Set authentication in security context
        OAuthBearerTokenAuthentication authentication = new OAuthBearerTokenAuthentication(user, token, scopes);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
