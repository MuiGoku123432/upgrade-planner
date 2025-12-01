package com.sentinovo.carbuildervin.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCalcResponseDto {
    private UUID buildId;
    private String buildName;
    private String vehicleLabel;
    private String currencyCode;

    // Applied filters (for display)
    private BudgetFiltersDto filters;

    // Totals
    private BigDecimal requiredCost;
    private BigDecimal optionalCost;
    private BigDecimal combinedCost;

    // Breakdowns
    private List<CategoryCostDto> byCategory;
    private List<TierCostDto> byTier;
    private List<MonthlyCostDto> byMonth;

    // Counts
    private long totalItemsCount;
    private long requiredItemsCount;
    private long optionalItemsCount;
}
