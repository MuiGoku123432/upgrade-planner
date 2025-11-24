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

    @Schema(description = "Part category code", example = "SUSPENSION")
    private String categoryCode;

    @Schema(description = "Part tier code", example = "OEM")
    private String tierCode;

    @Size(max = 500, message = "Link cannot exceed 500 characters")
    @Schema(description = "Product link URL", example = "https://example.com/subpart")
    private String productUrl;

    @Schema(description = "Part status", example = "PLANNED", allowableValues = {"PLANNED", "ORDERED", "SHIPPED", "DELIVERED", "INSTALLED", "CANCELLED"})
    private String status;

    @Schema(description = "Part price", example = "150.00")
    private java.math.BigDecimal price;

    @Schema(description = "Currency code", example = "USD")
    private String currencyCode;

    @Schema(description = "Whether this part is required", example = "true")
    private Boolean isRequired;

    @Schema(description = "Priority value for sorting (1-1000)", example = "100")
    private Integer priorityValue;

    @Schema(description = "Target purchase date", example = "2024-06-01")
    private java.time.LocalDate targetPurchaseDate;

    @Schema(description = "Sort order for display", example = "1")
    private Integer sortOrder;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Spring rate: 450 lbs/in")
    private String notes;
}