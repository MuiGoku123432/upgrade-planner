package com.sentinovo.carbuildervin.mcp.config;

import com.sentinovo.carbuildervin.mcp.resources.BuildResourceProvider;
import com.sentinovo.carbuildervin.mcp.resources.VehicleResourceProvider;
import com.sentinovo.carbuildervin.mcp.tools.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MCP Server configuration.
 * Registers all MCP tools and resources with the Spring AI MCP Server.
 */
@Slf4j
@Configuration
public class McpServerConfig {

    /**
     * Register all MCP tool providers.
     * Each tool class contains @Tool annotated methods that become MCP tools.
     */
    @Bean
    public List<ToolCallbackProvider> mcpToolCallbackProviders(
            VehicleMcpTools vehicleMcpTools,
            BuildMcpTools buildMcpTools,
            PartMcpTools partMcpTools,
            SubPartMcpTools subPartMcpTools,
            VinDecodeMcpTools vinDecodeMcpTools,
            LookupMcpTools lookupMcpTools,
            VehicleResourceProvider vehicleResourceProvider,
            BuildResourceProvider buildResourceProvider
    ) {
        log.info("Registering MCP tool providers");

        return List.of(
                MethodToolCallbackProvider.builder().toolObjects(vehicleMcpTools).build(),
                MethodToolCallbackProvider.builder().toolObjects(buildMcpTools).build(),
                MethodToolCallbackProvider.builder().toolObjects(partMcpTools).build(),
                MethodToolCallbackProvider.builder().toolObjects(subPartMcpTools).build(),
                MethodToolCallbackProvider.builder().toolObjects(vinDecodeMcpTools).build(),
                MethodToolCallbackProvider.builder().toolObjects(lookupMcpTools).build(),
                MethodToolCallbackProvider.builder().toolObjects(vehicleResourceProvider).build(),
                MethodToolCallbackProvider.builder().toolObjects(buildResourceProvider).build()
        );
    }
}
