package com.sentinovo.carbuildervin.mcp.tools;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeCreateDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeUpdateDto;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.mcp.security.McpUserContextProvider;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * MCP Tools for Build (Vehicle Upgrade) operations.
 * All operations are scoped to the authenticated user via API key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BuildMcpTools {

    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;
    private final McpUserContextProvider userContextProvider;

    @Tool(description = "List all builds (upgrade plans) for a specific vehicle")
    public List<VehicleUpgradeDto> listBuilds(
            @ToolParam(description = "The UUID of the vehicle") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Listing builds for vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership
        vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

        return vehicleUpgradeService.getVehicleUpgradesByVehicleId(UUID.fromString(vehicleId));
    }

    @Tool(description = "Get detailed information about a specific build by its ID")
    public VehicleUpgradeDto getBuild(
            @ToolParam(description = "The UUID of the build") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Getting build {} for user: {}", buildId, user.getUsername());

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));

        // Verify ownership via vehicle
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        return build;
    }

    @Tool(description = "Create a new build (upgrade plan) for a vehicle")
    public VehicleUpgradeDto createBuild(
            @ToolParam(description = "The UUID of the vehicle") String vehicleId,
            @ToolParam(description = "Name of the build") String name,
            @ToolParam(description = "Description of what this build involves") String description,
            @ToolParam(description = "Upgrade category ID (e.g., 1 for SUSPENSION, 2 for ENGINE)") Integer categoryId,
            @ToolParam(description = "Priority level (1-5, where 1 is highest)") Integer priorityLevel,
            @ToolParam(description = "Target completion date (YYYY-MM-DD format, optional)") String targetCompletionDate,
            @ToolParam(description = "Is this the primary build for this category?") Boolean isPrimaryForCategory
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Creating build for vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership
        vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

        VehicleUpgradeCreateDto createDto = new VehicleUpgradeCreateDto();
        createDto.setName(name);
        createDto.setDescription(description);
        createDto.setCategoryId(categoryId);
        createDto.setPriorityLevel(priorityLevel != null ? priorityLevel : 3);
        if (targetCompletionDate != null && !targetCompletionDate.isBlank()) {
            createDto.setTargetCompletionDate(LocalDate.parse(targetCompletionDate));
        }
        createDto.setIsPrimaryForCategory(isPrimaryForCategory != null ? isPrimaryForCategory : false);

        return vehicleUpgradeService.createVehicleUpgrade(UUID.fromString(vehicleId), createDto);
    }

    @Tool(description = "Update an existing build's information")
    public VehicleUpgradeDto updateBuild(
            @ToolParam(description = "The UUID of the build to update") String buildId,
            @ToolParam(description = "New name (optional)") String name,
            @ToolParam(description = "New description (optional)") String description,
            @ToolParam(description = "New category ID (optional)") Integer categoryId,
            @ToolParam(description = "New priority level 1-5 (optional)") Integer priorityLevel,
            @ToolParam(description = "New target completion date YYYY-MM-DD (optional)") String targetCompletionDate,
            @ToolParam(description = "New status: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED (optional)") String status,
            @ToolParam(description = "Is this the primary build for this category? (optional)") Boolean isPrimaryForCategory
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Updating build {} for user: {}", buildId, user.getUsername());

        // Verify ownership via the build's vehicle
        VehicleUpgradeDto existingBuild = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));
        vehicleService.verifyOwnership(existingBuild.getVehicleId(), user.getUsername());

        VehicleUpgradeUpdateDto updateDto = new VehicleUpgradeUpdateDto();
        updateDto.setName(name);
        updateDto.setDescription(description);
        updateDto.setCategoryId(categoryId);
        updateDto.setPriorityLevel(priorityLevel);
        if (targetCompletionDate != null && !targetCompletionDate.isBlank()) {
            updateDto.setTargetCompletionDate(LocalDate.parse(targetCompletionDate));
        }
        updateDto.setStatus(status);
        updateDto.setIsPrimaryForCategory(isPrimaryForCategory);

        return vehicleUpgradeService.updateVehicleUpgrade(UUID.fromString(buildId), updateDto);
    }

    @Tool(description = "Delete a build and all its associated parts")
    public String deleteBuild(
            @ToolParam(description = "The UUID of the build to delete") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Deleting build {} for user: {}", buildId, user.getUsername());

        // Verify ownership via the build's vehicle
        VehicleUpgradeDto existingBuild = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));
        vehicleService.verifyOwnership(existingBuild.getVehicleId(), user.getUsername());

        vehicleUpgradeService.deleteUpgrade(UUID.fromString(buildId));

        return "Build deleted successfully";
    }
}
