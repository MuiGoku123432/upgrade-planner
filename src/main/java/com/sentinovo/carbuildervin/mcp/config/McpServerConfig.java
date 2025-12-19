package com.sentinovo.carbuildervin.mcp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server configuration.
 *
 * Tool registration is handled automatically by the Spring AI MCP annotation scanner (fixed in 1.1.1).
 * All @McpTool annotated methods in @Component beans are auto-discovered.
 *
 * Configuration:
 * - spring.ai.mcp.server.annotation-scanner.enabled=true (in application.properties)
 */
@Slf4j
@Configuration
public class McpServerConfig {
    // Tool registration is handled by McpServerAnnotationScannerAutoConfiguration
}
