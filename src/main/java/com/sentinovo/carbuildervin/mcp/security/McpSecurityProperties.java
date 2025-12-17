package com.sentinovo.carbuildervin.mcp.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for MCP API key authentication.
 */
@Component
@ConfigurationProperties(prefix = "app.mcp")
public class McpSecurityProperties {

    /**
     * The HTTP header name containing the MCP API key.
     * Default: X-MCP-API-Key
     */
    private String apiKeyHeader = "X-MCP-API-Key";

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }
}
