package com.sentinovo.carbuildervin.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetFiltersDto {
    private boolean includeRequired;
    private boolean includeOptional;
    private List<String> categoryCodes;
    private List<String> tierCodes;
    private Integer minPriority;
    private Integer maxPriority;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> statuses;
}
