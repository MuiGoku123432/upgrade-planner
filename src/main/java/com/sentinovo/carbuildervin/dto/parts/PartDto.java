package com.sentinovo.carbuildervin.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete part information")
public class PartDto {

    @Schema(description = "Part ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID id;

    @Schema(description = "Vehicle upgrade ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID vehicleUpgradeId;

    @Schema(description = "Part category ID", example = "1")
    private Integer categoryId;

    @Schema(description = "Part category name", example = "Suspension")
    private String categoryName;

    @Schema(description = "Part tier ID", example = "2")
    private Integer tierId;

    @Schema(description = "Part tier name", example = "Premium")
    private String tierName;

    @Schema(description = "Part name", example = "Fox 2.5 Coilover")
    private String name;

    @Schema(description = "Part description", example = "Premium coilover suspension system")
    private String description;

    @Schema(description = "Brand name", example = "Fox Racing Shox")
    private String brand;

    @Schema(description = "Part number", example = "FOX-25-CO-001")
    private String partNumber;

    @Schema(description = "Product link URL", example = "https://example.com/product")
    private String link;

    @Schema(description = "Priority value for sorting", example = "100")
    private Integer priorityValue;

    @Schema(description = "Part status", example = "PLANNED")
    private String status;

    @Schema(description = "Part price", example = "2500.00")
    private BigDecimal price;

    @Schema(description = "Currency code", example = "USD")
    private String currencyCode;

    @Schema(description = "Quantity", example = "2")
    private Integer quantity;

    @Schema(description = "Additional notes", example = "Rear coilovers")
    private String notes;

    @Schema(description = "Creation timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime updatedAt;
}