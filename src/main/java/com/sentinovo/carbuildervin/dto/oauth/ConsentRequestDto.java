package com.sentinovo.carbuildervin.dto.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data for displaying the OAuth consent page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth consent page data")
public class ConsentRequestDto {

    @Schema(description = "The OAuth client name", example = "ChatGPT Desktop")
    private String clientName;

    @Schema(description = "The OAuth client ID", example = "chatgpt-desktop")
    private String clientId;

    @Schema(description = "Requested scopes as list")
    private List<String> requestedScopes;

    @Schema(description = "Scope descriptions for display")
    private List<ScopeDescription> scopeDescriptions;

    @Schema(description = "Redirect URI to use after consent")
    private String redirectUri;

    @Schema(description = "State parameter to preserve")
    private String state;

    @Schema(description = "Space-separated scope string")
    private String scope;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScopeDescription {
        private String scope;
        private String description;
    }
}
