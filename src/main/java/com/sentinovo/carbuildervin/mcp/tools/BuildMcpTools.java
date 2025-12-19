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
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
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

    @McpTool(name = "listBuilds",
            description = "List all builds (upgrade plans) for a specific vehicle",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public List<VehicleUpgradeDto> listBuilds(
            @McpToolParam(description = "The UUID of the vehicle") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Listing builds for vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership
        vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

        return vehicleUpgradeService.getVehicleUpgradesByVehicleId(UUID.fromString(vehicleId));
    }

    @McpTool(name = "getBuild",
            description = "Get detailed information about a specific build by its ID",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public VehicleUpgradeDto getBuild(
            @McpToolParam(description = "The UUID of the build") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Getting build {} for user: {}", buildId, user.getUsername());

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));

        // Verify ownership via vehicle
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        return build;
    }

    @McpTool(name = "createBuild",
            description = "Create a new build (upgrade plan) for a vehicle",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false))
    public VehicleUpgradeDto createBuild(
            @McpToolParam(description = "The UUID of the vehicle") String vehicleId,
            @McpToolParam(description = "Name of the build") String name,
            @McpToolParam(description = "Description of what this build involves") String description,
            @McpToolParam(description = "Upgrade category ID (e.g., 1 for SUSPENSION, 2 for ENGINE)") Integer categoryId,
            @McpToolParam(description = "Priority level (1-5, where 1 is highest)") Integer priorityLevel,
            @McpToolParam(description = "Target completion date (YYYY-MM-DD format, optional)") String targetCompletionDate,
            @McpToolParam(description = "Is this the primary build for this category?") Boolean isPrimaryForCategory
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

    @McpTool(name = "updateBuild",
            description = "Update an existing build's information",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false))
    public VehicleUpgradeDto updateBuild(
            @McpToolParam(description = "The UUID of the build to update") String buildId,
            @McpToolParam(description = "New name (optional)") String name,
            @McpToolParam(description = "New description (optional)") String description,
            @McpToolParam(description = "New category ID (optional)") Integer categoryId,
            @McpToolParam(description = "New priority level 1-5 (optional)") Integer priorityLevel,
            @McpToolParam(description = "New target completion date YYYY-MM-DD (optional)") String targetCompletionDate,
            @McpToolParam(description = "New status: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED (optional)") String status,
            @McpToolParam(description = "Is this the primary build for this category? (optional)") Boolean isPrimaryForCategory
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

    @McpTool(name = "deleteBuild",
            description = "Delete a build and all its associated parts",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true))
    public String deleteBuild(
            @McpToolParam(description = "The UUID of the build to delete") String buildId
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
