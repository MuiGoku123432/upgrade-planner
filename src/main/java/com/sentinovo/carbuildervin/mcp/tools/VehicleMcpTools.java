package com.sentinovo.carbuildervin.mcp.tools;

import com.sentinovo.carbuildervin.dto.vehicle.VehicleCreateDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleUpdateDto;
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
 * MCP Tools for Vehicle operations.
 * All operations are scoped to the authenticated user via API key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleMcpTools {

    private final VehicleService vehicleService;
    private final McpUserContextProvider userContextProvider;

    @Tool(description = "List all vehicles owned by the current user")
    public List<VehicleDto> listMyVehicles() {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Listing vehicles for user: {}", user.getUsername());
        return vehicleService.getUserVehicles(user.getId());
    }

    @Tool(description = "Get detailed information about a specific vehicle by its ID")
    public VehicleDto getVehicle(
            @ToolParam(description = "The UUID of the vehicle") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Getting vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership and return
        VehicleDto vehicle = vehicleService.getVehicleByIdAndOwnerUsername(
                UUID.fromString(vehicleId), user.getUsername());
        return vehicle;
    }

    @Tool(description = "Create a new vehicle. VIN is optional - provide it to auto-populate year/make/model from VIN decoding.")
    public VehicleDto createVehicle(
            @ToolParam(description = "Optional VIN (17 characters) - leave blank for project builds") String vin,
            @ToolParam(description = "Vehicle year (e.g., 2024)") Integer year,
            @ToolParam(description = "Vehicle make (e.g., Toyota)") String make,
            @ToolParam(description = "Vehicle model (e.g., 4Runner)") String model,
            @ToolParam(description = "Optional vehicle trim (e.g., TRD Pro)") String trim,
            @ToolParam(description = "Optional nickname for easy identification") String nickname,
            @ToolParam(description = "Optional notes about the vehicle") String notes
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Creating vehicle for user: {}", user.getUsername());

        VehicleCreateDto createDto = new VehicleCreateDto();
        createDto.setVin(vin);
        createDto.setYear(year);
        createDto.setMake(make);
        createDto.setModel(model);
        createDto.setTrim(trim);
        createDto.setNickname(nickname);
        createDto.setNotes(notes);

        return vehicleService.createVehicleForUsername(createDto, user.getUsername());
    }

    @Tool(description = "Update an existing vehicle's information")
    public VehicleDto updateVehicle(
            @ToolParam(description = "The UUID of the vehicle to update") String vehicleId,
            @ToolParam(description = "New VIN (optional)") String vin,
            @ToolParam(description = "New year (optional)") Integer year,
            @ToolParam(description = "New make (optional)") String make,
            @ToolParam(description = "New model (optional)") String model,
            @ToolParam(description = "New trim (optional)") String trim,
            @ToolParam(description = "New nickname (optional)") String nickname,
            @ToolParam(description = "New notes (optional)") String notes
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Updating vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership
        vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

        VehicleUpdateDto updateDto = new VehicleUpdateDto();
        updateDto.setVin(vin);
        updateDto.setYear(year);
        updateDto.setMake(make);
        updateDto.setModel(model);
        updateDto.setTrim(trim);
        updateDto.setNickname(nickname);
        updateDto.setNotes(notes);

        return vehicleService.updateVehicle(UUID.fromString(vehicleId), updateDto);
    }

    @Tool(description = "Archive a vehicle (soft delete - can be unarchived later)")
    public VehicleDto archiveVehicle(
            @ToolParam(description = "The UUID of the vehicle to archive") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Archiving vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership
        vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

        return vehicleService.setVehicleArchiveStatus(UUID.fromString(vehicleId), true);
    }

    @Tool(description = "Unarchive a previously archived vehicle")
    public VehicleDto unarchiveVehicle(
            @ToolParam(description = "The UUID of the vehicle to unarchive") String vehicleId
    ) {
        User user = userContextProvider.getCurrentUser();
        log.info("MCP: Unarchiving vehicle {} for user: {}", vehicleId, user.getUsername());

        // Verify ownership
        vehicleService.verifyOwnership(UUID.fromString(vehicleId), user.getUsername());

        return vehicleService.setVehicleArchiveStatus(UUID.fromString(vehicleId), false);
    }
}
