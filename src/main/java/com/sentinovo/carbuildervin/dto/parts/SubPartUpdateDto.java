package com.sentinovo.carbuildervin.dto.parts;

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
@Schema(description = "Request to update a sub-part")
public class SubPartUpdateDto {

    @Size(max = 200, message = "Sub-part name cannot exceed 200 characters")
    @Schema(description = "Sub-part name", example = "Coilover Spring")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Sub-part description", example = "High-performance spring component")
    private String description;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    @Schema(description = "Brand name", example = "Eibach")
    private String brand;

    @Size(max = 100, message = "Part number cannot exceed 100 characters")
    @Schema(description = "Part number", example = "EIB-SP-001")
    private String partNumber;

    @Size(max = 500, message = "Link cannot exceed 500 characters")
    @Schema(description = "Product link URL", example = "https://example.com/subpart")
    private String link;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Spring rate: 450 lbs/in")
    private String notes;
}