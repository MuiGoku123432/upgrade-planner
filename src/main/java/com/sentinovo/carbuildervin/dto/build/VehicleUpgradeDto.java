package com.sentinovo.carbuildervin.dto.build;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vehicle build/upgrade information")
public class VehicleUpgradeDto {

    @Schema(description = "Build ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID id;

    @Schema(description = "Vehicle ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID vehicleId;

    @Schema(description = "Upgrade category ID", example = "1")
    private Integer upgradeCategoryId;
    
    @Schema(description = "Upgrade category name", example = "Overlanding")
    private String upgradeCategoryName;

    @Schema(description = "Build name", example = "Overland Build v1")
    private String name;


    @Schema(description = "Build description", example = "Mild overland setup")
    private String description;

    @Schema(description = "Priority level", example = "1")
    private Integer priorityLevel;

    @Schema(description = "Target completion date", example = "2026-06-01")
    private LocalDate targetCompletionDate;

    @Schema(description = "Build status", example = "PLANNED")
    private String status;

    @Schema(description = "Whether this is the primary build for the category", example = "true")
    private Boolean isPrimaryForCategory;


    @Schema(description = "Creation timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime updatedAt;
}