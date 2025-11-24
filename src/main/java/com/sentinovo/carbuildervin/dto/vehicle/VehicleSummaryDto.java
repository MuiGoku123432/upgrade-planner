package com.sentinovo.carbuildervin.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight vehicle summary for lists")
public class VehicleSummaryDto {

    @Schema(description = "Vehicle ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID id;

    @Schema(description = "Vehicle Identification Number", example = "JTEVA5AR9S5004482")
    private String vin;

    @Schema(description = "Vehicle year", example = "2021")
    private Integer year;

    @Schema(description = "Vehicle make", example = "Toyota")
    private String make;

    @Schema(description = "Vehicle model", example = "4Runner")
    private String model;

    @Schema(description = "Vehicle trim level", example = "TRD Off Road")
    private String trim;

    @Schema(description = "User-defined nickname", example = "Trail Rig")
    private String nickname;

    @Schema(description = "Whether vehicle is archived", example = "false")
    private Boolean isArchived;

    @Schema(description = "Number of active builds", example = "3")
    private Long buildCount;

    @Schema(description = "Creation timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime createdAt;
}