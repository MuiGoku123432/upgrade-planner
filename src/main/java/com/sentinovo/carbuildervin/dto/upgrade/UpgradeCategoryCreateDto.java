package com.sentinovo.carbuildervin.dto.upgrade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new upgrade category")
public class UpgradeCategoryCreateDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Category name", example = "Performance")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Category description", example = "High-performance focused modifications")
    private String description;

    @Schema(description = "Whether category is active", example = "true")
    private Boolean isActive;
}