package com.sentinovo.carbuildervin.mcp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server configuration.
 *
 * Tool registration is handled automatically by the Spring AI MCP annotation scanner.
 * All @McpTool annotated methods in @Component beans are auto-discovered.
 *
 * Configuration:
 * - spring.ai.mcp.server.annotation-scanner.enabled=true (in application.properties)
 *
 * Tool classes auto-discovered:
 * - VehicleMcpTools
 * - BuildMcpTools
 * - PartMcpTools
 * - SubPartMcpTools
 * - VinDecodeMcpTools
 * - LookupMcpTools
 * - VehicleResourceProvider
 * - BuildResourceProvider
 */
@Slf4j
@Configuration
public class McpServerConfig {
    // Tool registration is now handled by McpServerAnnotationScannerAutoConfiguration
    // which discovers all @McpTool annotated methods in Spring beans.
}
