package com.sentinovo.carbuildervin.dto.vehicle;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to archive or unarchive a vehicle")
public class VehicleArchiveDto {

    @NotNull(message = "Archived status is required")
    @Schema(description = "Archive status", example = "true")
    private Boolean archived;

    @Schema(description = "Reason for archiving", example = "Vehicle sold")
    private String reason;
}