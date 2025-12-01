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
public class MonthlyCostDto {
    private String yearMonth;  // Format: "2025-03"
    private BigDecimal requiredCost;
    private BigDecimal optionalCost;
    private BigDecimal combinedCost;
    private long itemCount;
}
