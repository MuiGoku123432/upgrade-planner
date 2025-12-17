package com.sentinovo.carbuildervin.controller.parts;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.parts.PartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.PartUpdateDto;
import com.sentinovo.carbuildervin.dto.parts.csv.CsvImportResultDto;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.parts.csv.PartCsvImportService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Parts", description = "Part management operations")
public class PartController extends BaseController {

    private final PartService partService;
    private final PartCsvImportService partCsvImportService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;

    @Operation(
        summary = "List parts in build", 
        description = "Get paginated list of parts in a specific build"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Parts retrieved successfully",
        content = @Content(schema = @Schema(implementation = PageResponseDto.class))
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
    @GetMapping("/builds/{buildId}/parts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PageResponseDto<PartDto>>> getBuildParts(
            @PathVariable UUID buildId,
            @Parameter(description = "Pagination parameters") Pageable pageable,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting parts for build {} for user: {}", buildId, username);
        
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        PageResponseDto<PartDto> parts = partService.getPartsByUpgradeIdPaged(buildId, pageable);
        
        return successPage(parts);
    }

    @Operation(summary = "Get part by ID", description = "Get a specific part by its ID with sub-parts")
    @ApiResponse(
        responseCode = "200", 
        description = "Part retrieved successfully",
        content = @Content(schema = @Schema(implementation = PartDto.class))
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
    @GetMapping("/parts/{partId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PartDto>> getPart(
            @PathVariable UUID partId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting part {} for user: {}", partId, username);
        
        PartDto part = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(part.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        return success(part);
    }

    @Operation(summary = "Create part", description = "Create a new part in a build")
    @ApiResponse(
        responseCode = "201", 
        description = "Part created successfully",
        content = @Content(schema = @Schema(implementation = PartDto.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Build, category, or tier not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @ApiResponse(
        responseCode = "403", 
        description = "Access denied - not owner",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PostMapping("/builds/{buildId}/parts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PartDto>> createPart(
            @PathVariable UUID buildId,
            @Valid @RequestBody PartCreateDto createDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating part for build {} for user: {}", buildId, username);
        
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        PartDto part = partService.createPart(buildId, createDto);
        
        log.info("Part created successfully with ID: {}", part.getId());
        return created(part, "Part created successfully");
    }

    @Operation(
        summary = "Import parts from CSV",
        description = "Bulk import parts into a build from a CSV file. Supports partial import - valid rows are imported even if some rows fail validation."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Import completed (may include partial failures)",
            content = @Content(schema = @Schema(implementation = CsvImportResultDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file format or empty file",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Build not found",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - not owner",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
        )
    })
    @PostMapping(value = "/builds/{buildId}/parts/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<CsvImportResultDto>> importPartsFromCsv(
            @PathVariable UUID buildId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("CSV import for build {} by user: {}", buildId, username);

        // Validate file
        if (file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean isCsvByName = filename != null && filename.toLowerCase().endsWith(".csv");
        boolean isCsvByType = contentType != null &&
            (contentType.equals("text/csv") ||
             contentType.equals("application/vnd.ms-excel") ||
             contentType.equals("text/plain"));

        if (!isCsvByName && !isCsvByType) {
            throw new ValidationException("Invalid file type. Please upload a CSV file.");
        }

        // Verify ownership
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        vehicleService.verifyOwnership(build.getVehicleId(), username);

        // Perform import
        CsvImportResultDto result = partCsvImportService.importParts(buildId, file);

        String message;
        if (result.isFullSuccess()) {
            message = "All " + result.getSuccessCount() + " parts imported successfully";
        } else if (result.isPartialSuccess()) {
            message = String.format("Partial import: %d succeeded, %d failed",
                    result.getSuccessCount(), result.getFailureCount());
        } else {
            message = "Import failed - no parts were imported";
        }

        return success(result, message);
    }

    @Operation(summary = "Update part", description = "Update an existing part")
    @ApiResponse(
        responseCode = "200", 
        description = "Part updated successfully",
        content = @Content(schema = @Schema(implementation = PartDto.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
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
    @PatchMapping("/parts/{partId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PartDto>> updatePart(
            @PathVariable UUID partId,
            @Valid @RequestBody PartUpdateDto updateDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating part {} for user: {}", partId, username);
        
        PartDto currentPart = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(currentPart.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        PartDto part = partService.updatePart(partId, updateDto);
        
        log.info("Part updated successfully: {}", partId);
        return success(part, "Part updated successfully");
    }

    @Operation(summary = "Update part status", description = "Update the status of a part")
    @ApiResponse(
        responseCode = "200", 
        description = "Part status updated successfully",
        content = @Content(schema = @Schema(implementation = PartDto.class))
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
    @PatchMapping("/parts/{partId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<PartDto>> updatePartStatus(
            @PathVariable UUID partId,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating part {} status to {} for user: {}", partId, request.getStatus(), username);
        
        PartDto currentPart = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(currentPart.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        PartDto part = partService.updatePartStatusDto(partId, request.getStatus());
        
        log.info("Part status updated successfully: {}", partId);
        return success(part, "Part status updated successfully");
    }

    @Operation(summary = "Delete part", description = "Delete a part and all associated sub-parts")
    @ApiResponse(
        responseCode = "204", 
        description = "Part deleted successfully"
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
    @DeleteMapping("/parts/{partId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePart(
            @PathVariable UUID partId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Deleting part {} for user: {}", partId, username);
        
        PartDto currentPart = partService.getPartById(partId);
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(currentPart.getVehicleUpgradeId());
        vehicleService.verifyOwnership(build.getVehicleId(), username);
        
        partService.deletePart(partId);
        
        log.info("Part deleted successfully: {}", partId);
        return noContent();
    }

    @Schema(description = "Status update request")
    public static class StatusUpdateRequest {
        @jakarta.validation.constraints.NotNull(message = "Status cannot be null")
        @jakarta.validation.constraints.NotBlank(message = "Status cannot be blank")
        @jakarta.validation.constraints.Pattern(
            regexp = "^(PLANNED|ORDERED|SHIPPED|DELIVERED|INSTALLED|CANCELLED)$", 
            message = "Status must be one of: PLANNED, ORDERED, SHIPPED, DELIVERED, INSTALLED, CANCELLED"
        )
        @Schema(description = "New status", example = "ORDERED", allowableValues = {"PLANNED", "ORDERED", "SHIPPED", "DELIVERED", "INSTALLED", "CANCELLED"})
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}