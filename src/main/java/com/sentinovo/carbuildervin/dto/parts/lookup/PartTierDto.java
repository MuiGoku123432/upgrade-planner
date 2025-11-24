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
@Schema(description = "Part tier information")
public class PartTierDto {

    @Schema(description = "Tier code", example = "PREMIUM")
    private String code;

    @Schema(description = "Tier label", example = "Premium")
    private String label;

    @Schema(description = "Tier rank (lower = higher quality)", example = "3")
    private Integer rank;

    @Schema(description = "Tier description", example = "High-end parts")
    private String description;
}