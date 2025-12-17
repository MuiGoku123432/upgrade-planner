package com.sentinovo.carbuildervin.dto.parts.csv;

import com.sentinovo.carbuildervin.dto.parts.PartDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the result of a CSV parts import operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of CSV parts import operation")
public class CsvImportResultDto {

    @Schema(description = "Total number of rows processed (excluding header)", example = "10")
    private int totalRows;

    @Schema(description = "Number of parts successfully imported", example = "8")
    private int successCount;

    @Schema(description = "Number of rows that failed validation", example = "2")
    private int failureCount;

    @Schema(description = "List of successfully created parts")
    private List<PartDto> createdParts;

    @Schema(description = "List of row errors for failed imports")
    private List<CsvRowErrorDto> errors;

    /**
     * Check if all rows were imported successfully.
     */
    @Schema(description = "Whether all rows were imported successfully")
    public boolean isFullSuccess() {
        return failureCount == 0 && successCount > 0;
    }

    /**
     * Check if some but not all rows were imported successfully.
     */
    @Schema(description = "Whether some rows were imported and some failed")
    public boolean isPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }
}
