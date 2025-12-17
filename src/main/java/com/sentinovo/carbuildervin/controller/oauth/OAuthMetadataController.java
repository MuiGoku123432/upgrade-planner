package com.sentinovo.carbuildervin.controller.oauth;

import com.sentinovo.carbuildervin.config.OAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 Authorization Server Metadata endpoint (RFC 8414).
 * Allows MCP clients to discover OAuth configuration automatically.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "OAuth", description = "OAuth 2.0 Authorization endpoints")
public class OAuthMetadataController {

    private final OAuthProperties oAuthProperties;

    /**
     * OAuth 2.0 Authorization Server Metadata (RFC 8414)
     * MCP clients use this to discover OAuth endpoints.
     */
    @GetMapping(value = "/.well-known/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "OAuth server metadata", description = "Returns OAuth 2.0 authorization server metadata for client discovery")
    public Map<String, Object> oauthMetadata() {
        String issuer = oAuthProperties.getIssuer();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("issuer", issuer);
        metadata.put("authorization_endpoint", issuer + "/oauth/authorize");
        metadata.put("token_endpoint", issuer + "/oauth/token");
        metadata.put("revocation_endpoint", issuer + "/oauth/revoke");
        metadata.put("response_types_supported", List.of("code"));
        metadata.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
        metadata.put("token_endpoint_auth_methods_supported", List.of("client_secret_post", "none"));
        metadata.put("scopes_supported", List.of("mcp:read", "mcp:write"));
        metadata.put("code_challenge_methods_supported", List.of("plain", "S256"));

        return metadata;
    }
}
