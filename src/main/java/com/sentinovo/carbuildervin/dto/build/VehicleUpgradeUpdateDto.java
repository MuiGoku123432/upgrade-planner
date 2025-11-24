package com.sentinovo.carbuildervin.dto.build;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update vehicle build")
public class VehicleUpgradeUpdateDto {

    @Size(max = 200, message = "Name cannot exceed 200 characters")
    @Schema(description = "Build name", example = "Overland Build v2")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Build description", example = "Updated overland setup")
    private String description;

    @Min(value = 1, message = "Priority level must be at least 1")
    @Max(value = 10, message = "Priority level cannot exceed 10")
    @Schema(description = "Priority level (1-10)", example = "2")
    private Integer priorityLevel;

    @Schema(description = "Target completion date", example = "2026-08-01")
    private LocalDate targetCompletionDate;

    @Schema(description = "Build status", example = "IN_PROGRESS", allowableValues = {"PLANNED", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"})
    private String status;

    @Schema(description = "Whether this is the primary build for the category", example = "false")
    private Boolean isPrimaryForCategory;
}