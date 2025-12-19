package com.sentinovo.carbuildervin.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeSummaryDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.mcp.security.McpUserContextProvider;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MCP Resource provider for Build resources.
 * Provides read-only access to build data in a resource-oriented way.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BuildResourceProvider {

    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;
    private final PartService partService;
    private final McpUserContextProvider userContextProvider;
    private final ObjectMapper objectMapper;

    @McpTool(name = "getBuildResource",
            description = "Get a build resource by its URI (build://{buildId})",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String getBuildResource(
            @McpToolParam(description = "The build ID from the URI") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP Resource: Getting build {} for user: {}", buildId, user.getUsername());

        try {
            VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));

            // Verify ownership
            vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(build);
        } catch (Exception e) {
            log.error("Error fetching build resource: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @McpTool(name = "getVehicleBuildsResource",
            description = "Get all builds for a vehicle (builds://vehicle/{vehicleId})",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String getVehicleBuildsResource(
            @McpToolParam(description = "The vehicle ID") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP Resource: Getting builds for vehicle {} for user: {}", vehicleId, user.getUsername());

        try {
            // Verify ownership
            vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

            List<VehicleUpgradeDto> builds = vehicleUpgradeService.getVehicleUpgradesByVehicleId(
                    UUID.fromString(vehicleId));
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(builds);
        } catch (Exception e) {
            log.error("Error fetching builds resource: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @McpTool(name = "getBuildSummaryResource",
            description = "Get a build summary with cost breakdown (build://{buildId}/summary)",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String getBuildSummaryResource(
            @McpToolParam(description = "The build ID") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP Resource: Getting build summary {} for user: {}", buildId, user.getUsername());

        try {
            VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));

            // Verify ownership
            vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

            // Get parts and calculate costs
            List<PartDto> parts = partService.getPartsByUpgradeId(UUID.fromString(buildId));

            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal plannedCost = BigDecimal.ZERO;
            BigDecimal orderedCost = BigDecimal.ZERO;
            BigDecimal installedCost = BigDecimal.ZERO;

            Map<String, Integer> statusCounts = new HashMap<>();

            for (PartDto part : parts) {
                BigDecimal price = part.getPrice() != null ? part.getPrice() : BigDecimal.ZERO;
                totalCost = totalCost.add(price);

                String status = part.getStatus();
                statusCounts.merge(status, 1, Integer::sum);

                if ("PLANNED".equals(status)) {
                    plannedCost = plannedCost.add(price);
                } else if ("ORDERED".equals(status) || "SHIPPED".equals(status) || "DELIVERED".equals(status)) {
                    orderedCost = orderedCost.add(price);
                } else if ("INSTALLED".equals(status)) {
                    installedCost = installedCost.add(price);
                }
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("buildId", buildId);
            summary.put("buildName", build.getName());
            summary.put("status", build.getStatus());
            summary.put("categoryName", build.getUpgradeCategoryName());
            summary.put("totalParts", parts.size());
            summary.put("totalCost", totalCost);
            summary.put("plannedCost", plannedCost);
            summary.put("orderedCost", orderedCost);
            summary.put("installedCost", installedCost);
            summary.put("partsByStatus", statusCounts);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
        } catch (Exception e) {
            log.error("Error fetching build summary resource: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
