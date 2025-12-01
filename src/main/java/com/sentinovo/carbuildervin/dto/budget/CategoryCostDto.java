package com.sentinovo.carbuildervin.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCostDto {
    private String categoryCode;
    private String categoryLabel;
    private BigDecimal requiredCost;
    private BigDecimal optionalCost;
    private BigDecimal combinedCost;
    private long itemCount;
}
