package com.sentinovo.carbuildervin.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.mcp.security.McpUserContextProvider;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MCP Resource provider for Vehicle resources.
 * Provides read-only access to vehicle data in a resource-oriented way.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleResourceProvider {

    private final VehicleService vehicleService;
    private final McpUserContextProvider userContextProvider;
    private final ObjectMapper objectMapper;

    @Tool(description = "Get a vehicle resource by its URI (vehicle://{vehicleId})")
    public String getVehicleResource(
            @ToolParam(description = "The vehicle ID from the URI") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP Resource: Getting vehicle {} for user: {}", vehicleId, user.getUsername());

        try {
            VehicleDto vehicle = vehicleService.getVehicleByIdAndOwnerUsername(
                    UUID.fromString(vehicleId), user.getUsername());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vehicle);
        } catch (Exception e) {
            log.error("Error fetching vehicle resource: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @Tool(description = "Get all vehicles for the current user (vehicles://me)")
    public String getMyVehiclesResource() {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP Resource: Getting all vehicles for user: {}", user.getUsername());

        try {
            List<VehicleDto> vehicles = vehicleService.getUserVehicles(user.getId());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vehicles);
        } catch (Exception e) {
            log.error("Error fetching vehicles resource: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
