package com.sentinovo.carbuildervin.mcp.tools;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartUpdateDto;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.mcp.security.McpUserContextProvider;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.parts.SubPartService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * MCP Tools for Sub-Part operations.
 * Sub-parts are components that belong to a parent part.
 * All operations are scoped to the authenticated user via API key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubPartMcpTools {

    private final SubPartService subPartService;
    private final PartService partService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;
    private final McpUserContextProvider userContextProvider;

    @Tool(description = "List all sub-parts for a specific parent part")
    public List<SubPartDto> listSubParts(
            @ToolParam(description = "The UUID of the parent part") String partId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Listing sub-parts for part {} for user: {}", partId, user.getUsername());

        // Verify ownership via part -> build -> vehicle
        verifyPartOwnership(UUID.fromString(partId), user.getUsername());

        return subPartService.getSubPartsByParentPartId(UUID.fromString(partId));
    }

    @Tool(description = "Get detailed information about a specific sub-part by its ID")
    public SubPartDto getSubPart(
            @ToolParam(description = "The UUID of the sub-part") String subPartId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Getting sub-part {} for user: {}", subPartId, user.getUsername());

        SubPartDto subPart = subPartService.getSubPartById(UUID.fromString(subPartId));

        // Verify ownership via part -> build -> vehicle
        verifyPartOwnership(subPart.getPartId(), user.getUsername());

        return subPart;
    }

    @Tool(description = "Create a new sub-part for a parent part")
    public SubPartDto createSubPart(
            @ToolParam(description = "The UUID of the parent part") String partId,
            @ToolParam(description = "Name of the sub-part") String name,
            @ToolParam(description = "Description of the sub-part") String description,
            @ToolParam(description = "Price of the sub-part") Double price,
            @ToolParam(description = "Status: PLANNED, ORDERED, SHIPPED, DELIVERED, INSTALLED, CANCELLED") String status,
            @ToolParam(description = "Brand name (optional)") String brand,
            @ToolParam(description = "Part number (optional)") String partNumber,
            @ToolParam(description = "Product URL link (optional)") String productUrl
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Creating sub-part for part {} for user: {}", partId, user.getUsername());

        // Verify ownership via part -> build -> vehicle
        verifyPartOwnership(UUID.fromString(partId), user.getUsername());

        SubPartCreateDto createDto = new SubPartCreateDto();
        createDto.setName(name);
        createDto.setDescription(description);
        if (price != null) {
            createDto.setPrice(BigDecimal.valueOf(price));
        }
        createDto.setStatus(status != null ? status : "PLANNED");
        createDto.setBrand(brand);
        createDto.setPartNumber(partNumber);
        createDto.setProductUrl(productUrl);

        return subPartService.createSubPart(UUID.fromString(partId), createDto);
    }

    @Tool(description = "Update an existing sub-part's information")
    public SubPartDto updateSubPart(
            @ToolParam(description = "The UUID of the sub-part to update") String subPartId,
            @ToolParam(description = "New name (optional)") String name,
            @ToolParam(description = "New description (optional)") String description,
            @ToolParam(description = "New price (optional)") Double price,
            @ToolParam(description = "New status: PLANNED, ORDERED, SHIPPED, DELIVERED, INSTALLED, CANCELLED (optional)") String status,
            @ToolParam(description = "New brand (optional)") String brand,
            @ToolParam(description = "New part number (optional)") String partNumber,
            @ToolParam(description = "New product URL (optional)") String productUrl
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Updating sub-part {} for user: {}", subPartId, user.getUsername());

        // Verify ownership via sub-part -> part -> build -> vehicle
        SubPartDto existingSubPart = subPartService.getSubPartById(UUID.fromString(subPartId));
        verifyPartOwnership(existingSubPart.getPartId(), user.getUsername());

        SubPartUpdateDto updateDto = new SubPartUpdateDto();
        updateDto.setName(name);
        updateDto.setDescription(description);
        if (price != null) {
            updateDto.setPrice(BigDecimal.valueOf(price));
        }
        updateDto.setStatus(status);
        updateDto.setBrand(brand);
        updateDto.setPartNumber(partNumber);
        updateDto.setProductUrl(productUrl);

        return subPartService.updateSubPart(UUID.fromString(subPartId), updateDto);
    }

    @Tool(description = "Delete a sub-part")
    public String deleteSubPart(
            @ToolParam(description = "The UUID of the sub-part to delete") String subPartId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Deleting sub-part {} for user: {}", subPartId, user.getUsername());

        // Verify ownership via sub-part -> part -> build -> vehicle
        SubPartDto existingSubPart = subPartService.getSubPartById(UUID.fromString(subPartId));
        verifyPartOwnership(existingSubPart.getPartId(), user.getUsername());

        subPartService.deleteSubPart(UUID.fromString(subPartId));

        return "Sub-part deleted successfully";
    }

    private void verifyPartOwnership(UUID partId, String username) {
        PartDto part = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
    }
}
