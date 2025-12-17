package com.sentinovo.carbuildervin.mcp.security;

import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper component to get the current authenticated MCP user from security context.
 * Use this in MCP tools to get the user making the request.
 */
@Component
public class McpUserContextProvider {

    /**
     * Get the current authenticated user from MCP API key authentication.
     * @return the authenticated User
     * @throws UnauthorizedException if no valid MCP authentication
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof McpApiKeyAuthentication mcpAuth) {
            return mcpAuth.getUser();
        }

        throw new UnauthorizedException("No MCP API key authentication found");
    }

    /**
     * Get the current authenticated user's ID.
     * @return the user ID
     * @throws UnauthorizedException if no valid MCP authentication
     */
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get the current authenticated user's username.
     * @return the username
     * @throws UnauthorizedException if no valid MCP authentication
     */
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * Check if there is a valid MCP authentication in context.
     * @return true if authenticated via MCP API key
     */
    public boolean isMcpAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof McpApiKeyAuthentication;
    }
}
