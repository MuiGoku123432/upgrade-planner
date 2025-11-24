package com.sentinovo.carbuildervin.dto.parts.lookup;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new part category")
public class PartCategoryCreateDto {

    @NotBlank(message = "Category code is required")
    @Size(max = 20, message = "Code cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Code must contain only uppercase letters and underscores")
    @Schema(description = "Unique category code", example = "SUSPENSION")
    private String code;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Category name", example = "Suspension")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Category description", example = "Suspension system components")
    private String description;

    @Min(value = 1, message = "Priority value must be at least 1")
    @Max(value = 1000, message = "Priority value cannot exceed 1000")
    @Schema(description = "Priority value for sorting (1-1000)", example = "100")
    private Integer priorityValue;
}