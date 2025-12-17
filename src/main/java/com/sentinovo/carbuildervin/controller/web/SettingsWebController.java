package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Web controller for user settings page - MCP API key management
 */
@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsWebController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    /**
     * Main settings page
     */
    @GetMapping
    public String settingsPage(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading settings page for user: {}", currentUserId);

        populateApiKeyModel(model, currentUserId);

        return "settings";
    }

    /**
     * HTMX fragment - API key section
     */
    @GetMapping("/fragment/api-key")
    public String getApiKeyFragment(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading API key fragment for user: {}", currentUserId);

        populateApiKeyModel(model, currentUserId);

        return "settings/fragments/api-key-section :: api-key-section";
    }

    /**
     * Generate new MCP API key
     */
    @PostMapping("/api-key/generate")
    public String generateApiKey(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.info("Generating MCP API key for user: {}", currentUserId);

        String apiKey = userService.generateMcpApiKey(currentUserId);

        model.addAttribute("apiKey", apiKey);
        model.addAttribute("message", "API key generated successfully. Save this key - it won't be shown again!");

        return "settings/fragments/api-key-generated :: api-key-generated";
    }

    /**
     * Revoke MCP API key
     */
    @DeleteMapping("/api-key")
    public String revokeApiKey(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.info("Revoking MCP API key for user: {}", currentUserId);

        userService.revokeMcpApiKey(currentUserId);

        populateApiKeyModel(model, currentUserId);
        model.addAttribute("message", "API key revoked successfully");

        return "settings/fragments/api-key-section :: api-key-section";
    }

    /**
     * Helper to populate API key model attributes
     */
    private void populateApiKeyModel(Model model, UUID userId) {
        boolean hasApiKey = userService.hasMcpApiKey(userId);
        String maskedKey = userService.getMaskedMcpApiKey(userId);
        OffsetDateTime createdAt = userService.getMcpApiKeyCreatedAt(userId);

        model.addAttribute("hasApiKey", hasApiKey);
        model.addAttribute("maskedKey", maskedKey);
        model.addAttribute("apiKeyCreatedAt", createdAt);
    }
}
