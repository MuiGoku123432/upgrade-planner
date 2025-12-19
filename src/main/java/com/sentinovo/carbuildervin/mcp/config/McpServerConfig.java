package com.sentinovo.carbuildervin.mcp.config;

import com.sentinovo.carbuildervin.mcp.resources.BuildResourceProvider;
import com.sentinovo.carbuildervin.mcp.resources.VehicleResourceProvider;
import com.sentinovo.carbuildervin.mcp.tools.*;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.provider.tool.SyncMcpToolProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MCP Server configuration.
 * Manually registers @McpTool annotated beans using SyncMcpToolProvider.
 * This bypasses the annotation scanner which may have bugs in Spring AI 1.1.0.
 */
@Slf4j
@Configuration
public class McpServerConfig {

    /**
     * Create MCP tool specifications from all @McpTool annotated beans.
     * SyncMcpToolProvider properly extracts annotations including readOnlyHint and destructiveHint.
     */
    @Bean
    public List<SyncToolSpecification> mcpSyncToolSpecifications(
            VehicleMcpTools vehicleMcpTools,
            BuildMcpTools buildMcpTools,
            PartMcpTools partMcpTools,
            SubPartMcpTools subPartMcpTools,
            VinDecodeMcpTools vinDecodeMcpTools,
            LookupMcpTools lookupMcpTools,
            VehicleResourceProvider vehicleResourceProvider,
            BuildResourceProvider buildResourceProvider
    ) {
        log.info("Registering MCP tool specifications");

        List<Object> toolBeans = List.of(
                vehicleMcpTools,
                buildMcpTools,
                partMcpTools,
                subPartMcpTools,
                vinDecodeMcpTools,
                lookupMcpTools,
                vehicleResourceProvider,
                buildResourceProvider
        );

        SyncMcpToolProvider provider = new SyncMcpToolProvider(toolBeans);
        List<SyncToolSpecification> specs = provider.getToolSpecifications();

        log.info("Registered {} MCP tools", specs.size());
        specs.forEach(spec -> log.debug("Tool: {}", spec.tool().name()));

        return specs;
    }
}
