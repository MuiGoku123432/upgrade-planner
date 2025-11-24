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
@Schema(description = "Request to create a new part tier")
public class PartTierCreateDto {

    @NotBlank(message = "Tier code is required")
    @Size(max = 20, message = "Code cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Code must contain only uppercase letters and underscores")
    @Schema(description = "Unique tier code", example = "PREMIUM")
    private String code;

    @NotBlank(message = "Tier name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Tier name", example = "Premium")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Tier description", example = "High-end premium parts")
    private String description;

    @NotNull(message = "Rank is required")
    @Min(value = 1, message = "Rank must be at least 1")
    @Max(value = 100, message = "Rank cannot exceed 100")
    @Schema(description = "Tier rank (1=highest quality, 100=lowest)", example = "2")
    private Integer rank;
}