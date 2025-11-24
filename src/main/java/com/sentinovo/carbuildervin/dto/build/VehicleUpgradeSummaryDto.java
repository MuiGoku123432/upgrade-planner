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
@Schema(description = "Vehicle build summary for lists")
public class VehicleUpgradeSummaryDto {

    @Schema(description = "Build ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID id;

    @Schema(description = "Vehicle ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID vehicleId;

    @Schema(description = "Upgrade category name", example = "Overlanding")
    private String categoryName;

    @Schema(description = "Build name", example = "Overland Build v1")
    private String name;

    @Schema(description = "Build status", example = "PLANNED")
    private String status;

    @Schema(description = "Priority level", example = "1")
    private Integer priorityLevel;

    @Schema(description = "Target completion date", example = "2026-06-01")
    private LocalDate targetCompletionDate;

    @Schema(description = "Whether this is the primary build for the category", example = "true")
    private Boolean isPrimaryForCategory;

    @Schema(description = "Number of parts", example = "12")
    private Long partCount;

    @Schema(description = "Estimated total cost", example = "7500.00")
    private BigDecimal estimatedCost;

    @Schema(description = "Currency code", example = "USD")
    private String currencyCode;

    @Schema(description = "Creation timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime createdAt;
}