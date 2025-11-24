package com.sentinovo.carbuildervin.controller.vehicle;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeCreateDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeSummaryDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeUpdateDto;
import com.sentinovo.carbuildervin.dto.auth.UserDto;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.service.user.UserService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Vehicle Builds", description = "Vehicle build/upgrade management operations")
public class VehicleUpgradeController extends BaseController {

    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;
    private final UserService userService;

    @Operation(
        summary = "List builds for vehicle", 
        description = "Get all builds for a specific vehicle"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Vehicle builds retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Vehicle not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/vehicles/{vehicleId}/builds")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<List<VehicleUpgradeDto>>> getVehicleBuilds(
            @PathVariable UUID vehicleId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting builds for vehicle {} for user: {}", vehicleId, username);
        
        vehicleService.verifyOwnership(vehicleId, username);
        List<VehicleUpgradeDto> builds = vehicleUpgradeService.getVehicleUpgradesByVehicleId(vehicleId);
        
        return success(builds);
    }

    @Operation(summary = "Get build by ID", description = "Get a specific build by its ID with parts")
    @ApiResponse(
        responseCode = "200", 
        description = "Build retrieved successfully",
        content = @Content(schema = @Schema(implementation = VehicleUpgradeDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Build not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/builds/{buildId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleUpgradeDto>> getBuild(
            @PathVariable UUID buildId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting build {} for user: {}", buildId, username);
        
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        return success(build);
    }

    @Operation(summary = "Create build", description = "Create a new build for a vehicle")
    @ApiResponse(
        responseCode = "201", 
        description = "Build created successfully",
        content = @Content(schema = @Schema(implementation = VehicleUpgradeDto.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Vehicle or category not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PostMapping("/vehicles/{vehicleId}/builds")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleUpgradeDto>> createBuild(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody VehicleUpgradeCreateDto createDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating build for vehicle {} for user: {}", vehicleId, username);
        
        vehicleService.verifyOwnership(vehicleId, username);
        VehicleUpgradeDto build = vehicleUpgradeService.createVehicleUpgrade(vehicleId, createDto);
        
        log.info("Build created successfully with ID: {}", build.getId());
        return created(build, "Build created successfully");
    }

    @Operation(summary = "Update build", description = "Update an existing build")
    @ApiResponse(
        responseCode = "200", 
        description = "Build updated successfully",
        content = @Content(schema = @Schema(implementation = VehicleUpgradeDto.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Build not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PatchMapping("/builds/{buildId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleUpgradeDto>> updateBuild(
            @PathVariable UUID buildId,
            @Valid @RequestBody VehicleUpgradeUpdateDto updateDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating build {} for user: {}", buildId, username);
        
        VehicleUpgradeDto currentBuild = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(currentBuild.getVehicleId(), username);
        
        VehicleUpgradeDto build = vehicleUpgradeService.updateVehicleUpgrade(buildId, updateDto);
        
        log.info("Build updated successfully: {}", buildId);
        return success(build, "Build updated successfully");
    }

    @Operation(summary = "Delete build", description = "Delete a build and all associated parts")
    @ApiResponse(
        responseCode = "204", 
        description = "Build deleted successfully"
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Build not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @DeleteMapping("/builds/{buildId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBuild(
            @PathVariable UUID buildId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Deleting build {} for user: {}", buildId, username);
        
        VehicleUpgradeDto currentBuild = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(currentBuild.getVehicleId(), username);
        
        vehicleUpgradeService.deleteUpgrade(buildId);
        
        log.info("Build deleted successfully: {}", buildId);
        return noContent();
    }

    @Operation(
        summary = "Get build cost summary", 
        description = "Get cost breakdown for a build by category and tier"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Build summary retrieved successfully",
        content = @Content(schema = @Schema(implementation = VehicleUpgradeSummaryDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Build not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/builds/{buildId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<VehicleUpgradeSummaryDto>> getBuildSummary(
            @PathVariable UUID buildId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting build summary for build {} for user: {}", buildId, username);
        
        VehicleUpgradeDto currentBuild = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(currentBuild.getVehicleId(), username);
        
        VehicleUpgradeSummaryDto summary = vehicleUpgradeService.getVehicleUpgradeSummaryById(buildId);
        
        return success(summary);
    }

    @Operation(
        summary = "List all builds", 
        description = "Get paginated list of all builds for the current user"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Builds retrieved successfully",
        content = @Content(schema = @Schema(implementation = PageResponseDto.class))
    )
    @GetMapping("/builds")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PageResponseDto<VehicleUpgradeDto>>> getAllBuilds(
            @Parameter(description = "Pagination parameters") Pageable pageable,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting all builds for user: {}", username);
        
        UserDto user = userService.findByUsernameDto(username);
        PageResponseDto<VehicleUpgradeDto> builds = vehicleUpgradeService.getUserVehicleUpgradesPaged(user.getId(), pageable);
        
        return successPage(builds);
    }
}