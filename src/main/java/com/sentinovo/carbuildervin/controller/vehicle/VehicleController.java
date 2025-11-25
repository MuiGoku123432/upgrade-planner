package com.sentinovo.carbuildervin.controller.vehicle;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleCreateDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleUpdateDto;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Vehicles", description = "Vehicle management operations")
public class VehicleController extends BaseController {

    private final VehicleService vehicleService;

    @Operation(
        summary = "List user vehicles", 
        description = "Get a paginated list of vehicles owned by the current user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Vehicles retrieved successfully",
        content = @Content(schema = @Schema(implementation = PageResponseDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401", 
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PageResponseDto<VehicleDto>>> getVehicles(
            @Parameter(description = "Exact VIN match") @RequestParam(required = false) String vin,
            @Parameter(description = "Make filter") @RequestParam(required = false) String make,
            @Parameter(description = "Model filter") @RequestParam(required = false) String model,
            @Parameter(description = "Year filter") @RequestParam(required = false) Integer year,
            @Parameter(description = "Include archived vehicles") @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @Parameter(description = "Pagination parameters") Pageable pageable,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting vehicles for user: {} with filters - VIN: {}, make: {}, model: {}, year: {}, includeArchived: {}", 
                username, vin, make, model, year, includeArchived);
        
        PageResponseDto<VehicleDto> vehicles = vehicleService.getVehiclesByOwnerUsername(
                username, vin, make, model, year, includeArchived, pageable);
        
        return successPage(vehicles);
    }

    @Operation(summary = "Get vehicle by ID", description = "Get a specific vehicle by its ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Vehicle retrieved successfully",
        content = @Content(schema = @Schema(implementation = VehicleDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", 
        description = "Vehicle not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/{vehicleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleDto>> getVehicle(
            @PathVariable UUID vehicleId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting vehicle {} for user: {}", vehicleId, username);
        
        VehicleDto vehicle = vehicleService.getVehicleByIdAndOwnerUsername(vehicleId, username);
        
        return success(vehicle);
    }

    @Operation(summary = "Create vehicle", description = "Create a new vehicle (with or without VIN)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201", 
        description = "Vehicle created successfully",
        content = @Content(schema = @Schema(implementation = VehicleDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409", 
        description = "VIN already exists",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleDto>> createVehicle(
            @Valid @RequestBody VehicleCreateDto createDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating vehicle for user: {} with VIN: {}", username, createDto.getVin());
        
        VehicleDto vehicle = vehicleService.createVehicleForUsername(createDto, username);
        
        log.info("Vehicle created successfully with ID: {}", vehicle.getId());
        return created(vehicle, "Vehicle created successfully");
    }

    @Operation(summary = "Update vehicle", description = "Update an existing vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Vehicle updated successfully",
        content = @Content(schema = @Schema(implementation = VehicleDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", 
        description = "Vehicle not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PatchMapping("/{vehicleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleDto>> updateVehicle(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody VehicleUpdateDto updateDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating vehicle {} for user: {}", vehicleId, username);
        
        vehicleService.verifyOwnership(vehicleId, username);
        VehicleDto vehicle = vehicleService.updateVehicle(vehicleId, updateDto);
        
        log.info("Vehicle updated successfully: {}", vehicleId);
        return success(vehicle, "Vehicle updated successfully");
    }

    @Operation(summary = "Archive/Unarchive vehicle", description = "Change the archive status of a vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Vehicle archive status updated",
        content = @Content(schema = @Schema(implementation = VehicleDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", 
        description = "Vehicle not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PatchMapping("/{vehicleId}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleDto>> archiveVehicle(
            @PathVariable UUID vehicleId,
            @RequestBody ArchiveRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Changing archive status of vehicle {} to {} for user: {}", vehicleId, request.isArchived(), username);
        
        vehicleService.verifyOwnership(vehicleId, username);
        VehicleDto vehicle = vehicleService.setVehicleArchiveStatus(vehicleId, request.isArchived());
        
        String message = request.isArchived() ? "Vehicle archived successfully" : "Vehicle unarchived successfully";
        log.info("Vehicle archive status changed successfully: {}", vehicleId);
        return success(vehicle, message);
    }

    @Operation(summary = "Delete vehicle", description = "Delete a vehicle and all associated data")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "204", 
        description = "Vehicle deleted successfully"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", 
        description = "Vehicle not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable UUID vehicleId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Deleting vehicle {} for user: {}", vehicleId, username);
        
        vehicleService.verifyOwnership(vehicleId, username);
        vehicleService.archiveVehicle(vehicleId);
        
        log.info("Vehicle deleted successfully: {}", vehicleId);
        return noContent();
    }

    @Schema(description = "Archive status request")
    public static class ArchiveRequest {
        @Schema(description = "Whether to archive the vehicle", example = "true")
        private boolean archived;

        public boolean isArchived() {
            return archived;
        }

        public void setArchived(boolean archived) {
            this.archived = archived;
        }
    }
}