package com.sentinovo.carbuildervin.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetOverviewDto {
    private UUID buildId;
    private UUID vehicleId;
    private String vehicleLabel;
    private String buildName;
    private String upgradeCategoryName;
    private String status;
    private Integer priorityLevel;

    // Cost totals for this build
    private BigDecimal totalCost;
    private BigDecimal requiredCost;
    private BigDecimal optionalCost;

    // Item counts
    private long totalItemsCount;
    private long requiredItemsCount;
    private long optionalItemsCount;

    // Progress info (reuse from status)
    private double percentRequiredInstalled;
}
