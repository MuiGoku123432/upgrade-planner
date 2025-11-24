package com.sentinovo.carbuildervin.dto.parts.lookup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request to update a part tier")
public class PartTierUpdateDto {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Tier name", example = "Premium")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Tier description", example = "High-end premium parts")
    private String description;

    @Min(value = 1, message = "Rank must be at least 1")
    @Max(value = 100, message = "Rank cannot exceed 100")
    @Schema(description = "Tier rank (1=highest quality, 100=lowest)", example = "2")
    private Integer rank;

    @Schema(description = "Whether the tier is active", example = "true")
    private Boolean isActive;
}