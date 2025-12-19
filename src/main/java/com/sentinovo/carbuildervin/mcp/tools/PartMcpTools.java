package com.sentinovo.carbuildervin.mcp.tools;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.parts.PartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.PartUpdateDto;
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
import java.util.List;
import java.util.UUID;

/**
 * MCP Tools for Part operations.
 * All operations are scoped to the authenticated user via API key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartMcpTools {

    private final PartService partService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;
    private final McpUserContextProvider userContextProvider;

    @McpTool(name = "listParts",
            description = "List all parts in a specific build",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public List<PartDto> listParts(
            @McpToolParam(description = "The UUID of the build") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Listing parts for build {} for user: {}", buildId, user.getUsername());

        // Verify ownership via build's vehicle
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        return partService.getPartsByUpgradeId(UUID.fromString(buildId));
    }

    @McpTool(name = "getPart",
            description = "Get detailed information about a specific part by its ID",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public PartDto getPart(
            @McpToolParam(description = "The UUID of the part") String partId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Getting part {} for user: {}", partId, user.getUsername());

        PartDto part = partService.getPartById(UUID.fromString(partId));

        // Verify ownership via build's vehicle
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        return part;
    }

    @McpTool(name = "createPart",
            description = "Create a new part in a build",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false))
    public PartDto createPart(
            @McpToolParam(description = "The UUID of the build to add the part to") String buildId,
            @McpToolParam(description = "Name of the part") String name,
            @McpToolParam(description = "Part category code (e.g., SUSPENSION, ARMOR, WHEELS_TIRES)") String categoryCode,
            @McpToolParam(description = "Part tier code (e.g., BUDGET, MID, PREMIUM)") String tierCode,
            @McpToolParam(description = "Price of the part") Double price,
            @McpToolParam(description = "Status: PLANNED, ORDERED, SHIPPED, DELIVERED, INSTALLED, CANCELLED") String status,
            @McpToolParam(description = "Brand name (optional)") String brand,
            @McpToolParam(description = "Part number (optional)") String partNumber,
            @McpToolParam(description = "Product URL link (optional)") String productUrl,
            @McpToolParam(description = "Priority value for ordering (lower = higher priority)") Integer priorityValue,
            @McpToolParam(description = "Is this part required for the build?") Boolean isRequired
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Creating part for build {} for user: {}", buildId, user.getUsername());

        // Verify ownership via build's vehicle
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        PartCreateDto createDto = new PartCreateDto();
        createDto.setName(name);
        createDto.setCategoryCode(categoryCode);
        createDto.setTierCode(tierCode);
        if (price != null) {
            createDto.setPrice(BigDecimal.valueOf(price));
        }
        createDto.setStatus(status != null ? status : "PLANNED");
        createDto.setBrand(brand);
        createDto.setPartNumber(partNumber);
        createDto.setProductUrl(productUrl);
        createDto.setPriorityValue(priorityValue != null ? priorityValue : 50);
        createDto.setIsRequired(isRequired != null ? isRequired : false);

        return partService.createPart(UUID.fromString(buildId), createDto);
    }

    @McpTool(name = "updatePart",
            description = "Update an existing part's information",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false))
    public PartDto updatePart(
            @McpToolParam(description = "The UUID of the part to update") String partId,
            @McpToolParam(description = "New name (optional)") String name,
            @McpToolParam(description = "New category code (optional)") String categoryCode,
            @McpToolParam(description = "New tier code (optional)") String tierCode,
            @McpToolParam(description = "New price (optional)") Double price,
            @McpToolParam(description = "New status: PLANNED, ORDERED, SHIPPED, DELIVERED, INSTALLED, CANCELLED (optional)") String status,
            @McpToolParam(description = "New brand (optional)") String brand,
            @McpToolParam(description = "New part number (optional)") String partNumber,
            @McpToolParam(description = "New product URL (optional)") String productUrl,
            @McpToolParam(description = "New priority value (optional)") Integer priorityValue,
            @McpToolParam(description = "Is required? (optional)") Boolean isRequired
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Updating part {} for user: {}", partId, user.getUsername());

        // Verify ownership via build's vehicle
        PartDto existingPart = partService.getPartById(UUID.fromString(partId));
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(existingPart.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        PartUpdateDto updateDto = new PartUpdateDto();
        updateDto.setName(name);
        updateDto.setCategoryCode(categoryCode);
        updateDto.setTierCode(tierCode);
        if (price != null) {
            updateDto.setPrice(BigDecimal.valueOf(price));
        }
        updateDto.setStatus(status);
        updateDto.setBrand(brand);
        updateDto.setPartNumber(partNumber);
        updateDto.setProductUrl(productUrl);
        updateDto.setPriorityValue(priorityValue);
        updateDto.setIsRequired(isRequired);

        return partService.updatePart(UUID.fromString(partId), updateDto);
    }

    @McpTool(name = "deletePart",
            description = "Delete a part and all its associated sub-parts",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true))
    public String deletePart(
            @McpToolParam(description = "The UUID of the part to delete") String partId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Deleting part {} for user: {}", partId, user.getUsername());

        // Verify ownership via build's vehicle
        PartDto existingPart = partService.getPartById(UUID.fromString(partId));
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(existingPart.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        partService.deletePart(UUID.fromString(partId));

        return "Part deleted successfully";
    }

    @McpTool(name = "calculateBuildCost",
            description = "Calculate the total cost of all parts in a build",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String calculateBuildCost(
            @McpToolParam(description = "The UUID of the build") String buildId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Calculating cost for build {} for user: {}", buildId, user.getUsername());

        // Verify ownership via build's vehicle
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));
        vehicleService.verifyOwnership(build.getVehicleId(), user.getUsername());

        List<PartDto> parts = partService.getPartsByUpgradeId(UUID.fromString(buildId));

        BigDecimal totalCost = BigDecimal.ZERO;
        int partCount = 0;
        int plannedCount = 0;
        int orderedCount = 0;
        int installedCount = 0;

        for (PartDto part : parts) {
            if (part.getPrice() != null) {
                totalCost = totalCost.add(part.getPrice());
            }
            partCount++;

            if ("PLANNED".equals(part.getStatus())) plannedCount++;
            else if ("ORDERED".equals(part.getStatus()) || "SHIPPED".equals(part.getStatus())) orderedCount++;
            else if ("INSTALLED".equals(part.getStatus())) installedCount++;
        }

        return String.format(
            "Build Cost Summary for '%s':\n" +
            "- Total Parts: %d\n" +
            "- Total Cost: $%.2f\n" +
            "- Planned: %d\n" +
            "- Ordered/Shipped: %d\n" +
            "- Installed: %d",
            build.getName(), partCount, totalCost, plannedCount, orderedCount, installedCount
        );
    }
}
