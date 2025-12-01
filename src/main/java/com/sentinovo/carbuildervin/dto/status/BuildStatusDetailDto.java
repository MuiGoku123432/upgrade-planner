package com.sentinovo.carbuildervin.dto.status;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed build status with parts grouped by status")
public class BuildStatusDetailDto {

    @Schema(description = "Build information")
    private VehicleUpgradeDto build;

    @Schema(description = "Vehicle label (year make model or nickname)")
    private String vehicleLabel;

    @Schema(description = "Parts grouped by status (PLANNED, ORDERED, INSTALLED, etc.)")
    private Map<String, List<PartDto>> partsByStatus;

    @Schema(description = "Total parts count")
    private long totalPartsCount;

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
}
