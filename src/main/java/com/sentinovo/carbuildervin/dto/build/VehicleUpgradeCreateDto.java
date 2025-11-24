package com.sentinovo.carbuildervin.dto.build;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request to create a new vehicle build")
public class VehicleUpgradeCreateDto {

    @NotNull(message = "Upgrade category ID is required")
    @Schema(description = "Upgrade category ID", example = "1")
    private Integer upgradeCategoryId;

    @NotBlank(message = "Build name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    @Schema(description = "Build name", example = "Overland Build v1")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Build description", example = "Intro overland setup")
    private String description;

    @Min(value = 1, message = "Priority level must be at least 1")
    @Max(value = 10, message = "Priority level cannot exceed 10")
    @Schema(description = "Priority level (1-10)", example = "1")
    private Integer priorityLevel;

    @Future(message = "Target completion date must be in the future")
    @Schema(description = "Target completion date", example = "2026-06-01")
    private LocalDate targetCompletionDate;

    @Schema(description = "Build status", example = "PLANNED", allowableValues = {"PLANNED", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"})
    private String status;

    @Schema(description = "Whether this is the primary build for the category", example = "true")
    private Boolean isPrimaryForCategory;
}