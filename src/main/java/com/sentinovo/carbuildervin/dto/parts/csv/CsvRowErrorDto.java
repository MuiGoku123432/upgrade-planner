package com.sentinovo.carbuildervin.dto.parts.csv;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO representing error details for a single CSV row that failed validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error details for a single CSV row")
public class CsvRowErrorDto {

    @Schema(description = "Row number in the CSV file (1-indexed, excluding header)", example = "3")
    private int rowNumber;

    @Schema(description = "Original row data as key-value pairs")
    private Map<String, String> rowData;

    @Schema(description = "List of validation errors for this row")
    private List<String> errors;
}
