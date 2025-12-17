package com.sentinovo.carbuildervin.mcp.security;

import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that authenticates MCP requests using API key header.
 * Only processes requests to /api/v1/mcp/** endpoints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final McpSecurityProperties mcpSecurityProperties;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Only process MCP endpoints
        if (!requestPath.startsWith("/api/v1/mcp")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get API key from header
        String apiKey = request.getHeader(mcpSecurityProperties.getApiKeyHeader());

        if (apiKey == null || apiKey.isBlank()) {
            log.debug("No MCP API key provided for request: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"MCP API key required\",\"message\":\"Provide API key in " +
                    mcpSecurityProperties.getApiKeyHeader() + " header\"}");
            return;
        }

        // Look up user by API key
        Optional<User> userOpt = userService.findByMcpApiKey(apiKey);

        if (userOpt.isEmpty()) {
            log.warn("Invalid MCP API key provided for request: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid API key\",\"message\":\"The provided MCP API key is invalid or expired\"}");
            return;
        }

        User user = userOpt.get();
        log.debug("MCP API key authenticated for user: {}", user.getUsername());

        // Set authentication in security context
        McpApiKeyAuthentication authentication = new McpApiKeyAuthentication(user, apiKey);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
