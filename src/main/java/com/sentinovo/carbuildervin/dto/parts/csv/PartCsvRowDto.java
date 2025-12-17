package com.sentinovo.carbuildervin.dto.parts.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a single row from a parts CSV file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartCsvRowDto {

    private String name;
    private String categoryCode;
    private String tierCode;
    private BigDecimal price;
    private String status;
    private String brand;
    private String productUrl;
    private Integer priorityValue;
    private Boolean isRequired;
}
