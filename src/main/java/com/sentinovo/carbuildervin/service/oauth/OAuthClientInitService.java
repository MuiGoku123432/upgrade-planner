package com.sentinovo.carbuildervin.service.oauth;

import com.sentinovo.carbuildervin.entities.oauth.OAuthClient;
import com.sentinovo.carbuildervin.repository.oauth.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for initializing default OAuth clients.
 * Pre-registers known MCP clients (ChatGPT Desktop, Claude Desktop, etc.)
 * so users don't need to configure anything manually.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthClientInitService {

    private final OAuthClientRepository clientRepository;

    /**
     * Ensures default OAuth clients are registered.
     * Called on application startup.
     */
    @Transactional
    public void ensureDefaultClients() {
        registerClient(
            "chatgpt-desktop",
            "ChatGPT Desktop",
            "[\"https://chat.openai.com/aip/g/callback\", \"https://chatgpt.com/aip/g/callback\", \"http://localhost:*\", \"http://127.0.0.1:*\"]"
        );

        registerClient(
            "claude-desktop",
            "Claude Desktop",
            "[\"http://localhost:*\", \"http://127.0.0.1:*\"]"
        );

        registerClient(
            "generic-mcp-client",
            "Generic MCP Client",
            "[\"http://localhost:*\", \"http://127.0.0.1:*\"]"
        );
    }

    private void registerClient(String clientId, String clientName, String redirectUris) {
        if (clientRepository.existsByClientId(clientId)) {
            log.debug("OAuth client '{}' already exists", clientId);
            return;
        }

        OAuthClient client = OAuthClient.builder()
                .clientId(clientId)
                .clientSecret("public-client") // Public client - secret not verified
                .clientName(clientName)
                .redirectUris(redirectUris)
                .scopes("mcp:read mcp:write")
                .isConfidential(false) // Public client - no secret verification needed
                .isActive(true)
                .build();

        clientRepository.save(client);
        log.info("Registered OAuth client: {}", clientId);
    }
}
