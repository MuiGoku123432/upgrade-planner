package com.sentinovo.carbuildervin.controller.parts;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartDto;
import com.sentinovo.carbuildervin.dto.parts.SubPartUpdateDto;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.parts.SubPartService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "Sub-Parts", description = "Sub-part management operations")
public class SubPartController extends BaseController {

    private final SubPartService subPartService;
    private final PartService partService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;

    @Operation(
        summary = "List sub-parts for part", 
        description = "Get all sub-parts for a specific part"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Sub-parts retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Part not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/parts/{partId}/sub-parts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<List<SubPartDto>>> getPartSubParts(
            @PathVariable UUID partId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting sub-parts for part {} for user: {}", partId, username);
        
        PartDto part = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        List<SubPartDto> subParts = subPartService.getSubPartsByParentPartId(partId);
        
        return success(subParts);
    }

    @Operation(summary = "Get sub-part by ID", description = "Get a specific sub-part by its ID")
    @ApiResponse(
        responseCode = "200", 
        description = "Sub-part retrieved successfully",
        content = @Content(schema = @Schema(implementation = SubPartDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Sub-part not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/sub-parts/{subPartId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<SubPartDto>> getSubPart(
            @PathVariable UUID subPartId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting sub-part {} for user: {}", subPartId, username);
        
        SubPartDto subPart = subPartService.getSubPartById(subPartId);
        PartDto part = partService.getPartById(subPart.getPartId());
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        return success(subPart);
    }

    @Operation(summary = "Create sub-part", description = "Create a new sub-part for a part")
    @ApiResponse(
        responseCode = "201", 
        description = "Sub-part created successfully",
        content = @Content(schema = @Schema(implementation = SubPartDto.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Part, category, or tier not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PostMapping("/parts/{partId}/sub-parts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<SubPartDto>> createSubPart(
            @PathVariable UUID partId,
            @Valid @RequestBody SubPartCreateDto createDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating sub-part for part {} for user: {}", partId, username);
        
        PartDto part = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        SubPartDto subPart = subPartService.createSubPart(partId, createDto);
        
        log.info("Sub-part created successfully with ID: {}", subPart.getId());
        return created(subPart, "Sub-part created successfully");
    }

    @Operation(summary = "Update sub-part", description = "Update an existing sub-part")
    @ApiResponse(
        responseCode = "200", 
        description = "Sub-part updated successfully",
        content = @Content(schema = @Schema(implementation = SubPartDto.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Sub-part not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PatchMapping("/sub-parts/{subPartId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<SubPartDto>> updateSubPart(
            @PathVariable UUID subPartId,
            @Valid @RequestBody SubPartUpdateDto updateDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating sub-part {} for user: {}", subPartId, username);
        
        SubPartDto currentSubPart = subPartService.getSubPartById(subPartId);
        PartDto part = partService.getPartById(currentSubPart.getPartId());
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        SubPartDto subPart = subPartService.updateSubPart(subPartId, updateDto);
        
        log.info("Sub-part updated successfully: {}", subPartId);
        return success(subPart, "Sub-part updated successfully");
    }

    @Operation(summary = "Update sub-part status", description = "Update the status of a sub-part")
    @ApiResponse(
        responseCode = "200", 
        description = "Sub-part status updated successfully",
        content = @Content(schema = @Schema(implementation = SubPartDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Sub-part not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PatchMapping("/sub-parts/{subPartId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<SubPartDto>> updateSubPartStatus(
            @PathVariable UUID subPartId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating sub-part {} status to {} for user: {}", subPartId, request.getStatus(), username);
        
        SubPartDto currentSubPart = subPartService.getSubPartById(subPartId);
        PartDto part = partService.getPartById(currentSubPart.getPartId());
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        SubPartDto subPart = subPartService.updateSubPartStatusDto(subPartId, request.getStatus());
        
        log.info("Sub-part status updated successfully: {}", subPartId);
        return success(subPart, "Sub-part status updated successfully");
    }

    @Operation(summary = "Delete sub-part", description = "Delete a sub-part")
    @ApiResponse(
        responseCode = "204", 
        description = "Sub-part deleted successfully"
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Sub-part not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @DeleteMapping("/sub-parts/{subPartId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSubPart(
            @PathVariable UUID subPartId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Deleting sub-part {} for user: {}", subPartId, username);
        
        SubPartDto currentSubPart = subPartService.getSubPartById(subPartId);
        PartDto part = partService.getPartById(currentSubPart.getPartId());
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        subPartService.deleteSubPart(subPartId);
        
        log.info("Sub-part deleted successfully: {}", subPartId);
        return noContent();
    }

    @Schema(description = "Status update request")
    public static class StatusUpdateRequest {
        @Schema(description = "New status", example = "ORDERED")
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}