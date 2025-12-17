package com.sentinovo.carbuildervin.mcp.security;

import com.sentinovo.carbuildervin.entities.user.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Authentication token for OAuth 2.0 Bearer token authentication.
 * Contains the authenticated user as the principal and the scopes granted.
 */
public class OAuthBearerTokenAuthentication extends AbstractAuthenticationToken {

    private final User user;
    private final String token;
    private final String scopes;

    public OAuthBearerTokenAuthentication(User user, String token, String scopes) {
        super(extractAuthorities(user));
        this.user = user;
        this.token = token;
        this.scopes = scopes;
        setAuthenticated(true);
    }

    private static Collection<? extends GrantedAuthority> extractAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return token;
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

    /**
     * Get the OAuth scopes granted to this token.
     */
    public String getScopes() {
        return scopes;
    }

    /**
     * Check if a specific scope is granted.
     */
    public boolean hasScope(String scope) {
        if (scopes == null) return false;
        for (String s : scopes.split("\\s+")) {
            if (s.equals(scope)) return true;
        }
        return false;
    }
}
