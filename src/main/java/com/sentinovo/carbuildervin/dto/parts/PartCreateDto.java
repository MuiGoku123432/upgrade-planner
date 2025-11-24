package com.sentinovo.carbuildervin.dto.parts;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new part")
public class PartCreateDto {

    @NotNull(message = "Part category code is required")
    @Schema(description = "Part category code", example = "SUSPENSION")
    private String categoryCode;

    @Schema(description = "Part tier code", example = "OEM")
    private String tierCode;

    @NotBlank(message = "Part name is required")
    @Size(max = 200, message = "Part name cannot exceed 200 characters")
    @Schema(description = "Part name", example = "Fox 2.5 Coilover")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Part description", example = "Premium coilover suspension system")
    private String description;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    @Schema(description = "Brand name", example = "Fox Racing Shox")
    private String brand;

    @Size(max = 100, message = "Part number cannot exceed 100 characters")
    @Schema(description = "Part number", example = "FOX-25-CO-001")
    private String partNumber;

    @Size(max = 500, message = "Product URL cannot exceed 500 characters")
    @Schema(description = "Product link URL", example = "https://example.com/product")
    private String productUrl;
    
    @Schema(description = "Whether this part is required", example = "true")
    private Boolean isRequired;
    
    @Schema(description = "Target purchase date", example = "2024-06-01")
    private java.time.LocalDate targetPurchaseDate;
    
    @Schema(description = "Sort order for display", example = "1")
    private Integer sortOrder;

    @Min(value = 1, message = "Priority value must be at least 1")
    @Max(value = 1000, message = "Priority value cannot exceed 1000")
    @Schema(description = "Priority value for sorting (1-1000)", example = "100")
    private Integer priorityValue;

    @Schema(description = "Part status", example = "PLANNED", allowableValues = {"PLANNED", "ORDERED", "SHIPPED", "DELIVERED", "INSTALLED", "CANCELLED"})
    private String status;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits before decimal and 2 after")
    @Schema(description = "Part price", example = "2500.00")
    private BigDecimal price;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Schema(description = "Currency code", example = "USD")
    private String currencyCode;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity", example = "2")
    private Integer quantity;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Rear coilovers")
    private String notes;
}