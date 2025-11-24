package com.sentinovo.carbuildervin.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sub-part information")
public class SubPartDto {

    @Schema(description = "Sub-part ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID id;

    @Schema(description = "Parent part ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID partId;

    @Schema(description = "Sub-part name", example = "Coilover Spring")
    private String name;

    @Schema(description = "Sub-part description", example = "High-performance spring component")
    private String description;

    @Schema(description = "Brand name", example = "Eibach")
    private String brand;

    @Schema(description = "Part number", example = "EIB-SP-001")
    private String partNumber;

    @Schema(description = "Product link URL", example = "https://example.com/subpart")
    private String link;

    @Schema(description = "Additional notes", example = "Spring rate: 450 lbs/in")
    private String notes;

    @Schema(description = "Creation timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime updatedAt;
}