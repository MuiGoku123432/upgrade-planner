package com.sentinovo.carbuildervin.dto.status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Build status overview for the status page")
public class BuildStatusOverviewDto {

    @Schema(description = "Build ID")
    private UUID buildId;

    @Schema(description = "Vehicle ID")
    private UUID vehicleId;

    @Schema(description = "Vehicle label (year make model or nickname)")
    private String vehicleLabel;

    @Schema(description = "Upgrade category ID")
    private Integer upgradeCategoryId;

    @Schema(description = "Upgrade category key")
    private String upgradeCategoryKey;

    @Schema(description = "Upgrade category name")
    private String upgradeCategoryName;

    @Schema(description = "Build name")
    private String buildName;

    @Schema(description = "Build status")
    private String status;

    @Schema(description = "Priority level")
    private Integer priorityLevel;

    @Schema(description = "Target completion date")
    private LocalDate targetCompletionDate;

    @Schema(description = "Total required parts count")
    private long requiredPartsTotal;

    @Schema(description = "Installed required parts count")
    private long requiredPartsInstalled;

    @Schema(description = "Total optional parts count")
    private long optionalPartsTotal;

    @Schema(description = "Installed optional parts count")
    private long optionalPartsInstalled;

    @Schema(description = "Percentage of required parts installed")
    private double percentRequiredInstalled;

    @Schema(description = "Percentage of optional parts installed")
    private double percentOptionalInstalled;

    @Schema(description = "Count of overdue required parts")
    private long overdueRequiredCount;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private OffsetDateTime updatedAt;
}
