package com.sentinovo.carbuildervin.dto.upgrade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upgrade category information")
public class UpgradeCategoryDto {

    @Schema(description = "Category ID", example = "1")
    private Integer id;

    @Schema(description = "Category key", example = "OVERLANDING")
    private String key;

    @Schema(description = "Category name", example = "Overlanding")
    private String name;

    @Schema(description = "Category description", example = "Camping and trail capability focused build")
    private String description;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;

    @Schema(description = "Whether category is active", example = "true")
    private Boolean isActive;
}