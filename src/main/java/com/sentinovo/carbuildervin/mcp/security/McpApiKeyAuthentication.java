package com.sentinovo.carbuildervin.mcp.security;

import com.sentinovo.carbuildervin.entities.user.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Authentication token for MCP API key authentication.
 * Contains the authenticated user as the principal.
 */
public class McpApiKeyAuthentication extends AbstractAuthenticationToken {

    private final User user;
    private final String apiKey;

    public McpApiKeyAuthentication(User user, String apiKey) {
        super(extractAuthorities(user));
        this.user = user;
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    private static Collection<? extends GrantedAuthority> extractAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    /**
     * Get the authenticated user.
     */
    public User getUser() {
        return user;
    }
}
