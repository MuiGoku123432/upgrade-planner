package com.sentinovo.carbuildervin.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLineItemDto {
    private UUID id;
    private String type;           // "PART" or "SUB_PART"
    private UUID parentPartId;     // null for parts, parent ID for sub-parts
    private String name;
    private String brand;
    private String categoryCode;
    private String categoryLabel;
    private String tierCode;
    private String tierLabel;
    private Boolean isRequired;
    private Integer priorityValue;
    private LocalDate targetPurchaseDate;
    private String status;
    private BigDecimal price;
    private String currencyCode;
}
