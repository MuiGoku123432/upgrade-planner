package com.sentinovo.carbuildervin.dto.parts.lookup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Part category information")
public class PartCategoryDto {

    @Schema(description = "Category code", example = "SUSPENSION")
    private String code;

    @Schema(description = "Category label", example = "Suspension")
    private String label;

    @Schema(description = "Category description", example = "Coils, shocks, leaf springs, control arms")
    private String description;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;
}