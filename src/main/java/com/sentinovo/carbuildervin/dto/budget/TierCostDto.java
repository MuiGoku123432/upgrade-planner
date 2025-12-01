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
public class TierCostDto {
    private String tierCode;
    private String tierLabel;
    private BigDecimal cost;
    private long itemCount;
}
