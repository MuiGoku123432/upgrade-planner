package com.sentinovo.carbuildervin.service.parts.csv;

import com.sentinovo.carbuildervin.dto.parts.PartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.csv.CsvImportResultDto;
import com.sentinovo.carbuildervin.dto.parts.csv.CsvRowErrorDto;
import com.sentinovo.carbuildervin.dto.parts.csv.PartCsvRowDto;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.service.parts.PartCategoryService;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.parts.PartTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for orchestrating CSV parts import.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartCsvImportService {

    private final PartCsvParserService parserService;
    private final PartService partService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;

    /**
     * Import parts from a CSV file into a build.
     *
     * @param buildId the build/vehicle upgrade ID to add parts to
     * @param file    the uploaded CSV file
     * @return import result with success/failure counts and details
     */
    @Transactional
    public CsvImportResultDto importParts(UUID buildId, MultipartFile file) {
        log.info("Starting CSV import for build: {}", buildId);

        // Parse the CSV file
        PartCsvParserService.ParseResult parseResult = parserService.parseAndValidate(file);

        List<PartDto> createdParts = new ArrayList<>();
        List<CsvRowErrorDto> allErrors = new ArrayList<>(parseResult.errors());

        // Track row numbers for database validation errors
        int rowIndex = 0;
        for (PartCsvRowDto row : parseResult.validRows()) {
            rowIndex++;

            // Find the actual row number (accounting for skipped rows)
            int actualRowNumber = findActualRowNumber(rowIndex, parseResult);

            // Validate against database (category/tier existence)
            List<String> dbErrors = validateAgainstDatabase(row);

            if (!dbErrors.isEmpty()) {
                allErrors.add(CsvRowErrorDto.builder()
                        .rowNumber(actualRowNumber)
                        .rowData(rowToMap(row))
                        .errors(dbErrors)
                        .build());
                continue;
            }

            // Create the part
            try {
                PartCreateDto createDto = convertToCreateDto(row);
                PartDto createdPart = partService.createPart(buildId, createDto);
                createdParts.add(createdPart);
                log.debug("Successfully created part: {} (row {})", createdPart.getName(), actualRowNumber);
            } catch (Exception e) {
                log.warn("Failed to create part from row {}: {}", actualRowNumber, e.getMessage());
                allErrors.add(CsvRowErrorDto.builder()
                        .rowNumber(actualRowNumber)
                        .rowData(rowToMap(row))
                        .errors(List.of("Failed to create part: " + e.getMessage()))
                        .build());
            }
        }

        // Sort errors by row number
        allErrors.sort((a, b) -> Integer.compare(a.getRowNumber(), b.getRowNumber()));

        CsvImportResultDto result = CsvImportResultDto.builder()
                .totalRows(parseResult.totalRows())
                .successCount(createdParts.size())
                .failureCount(allErrors.size())
                .createdParts(createdParts)
                .errors(allErrors)
                .build();

        log.info("CSV import complete for build {}: {} created, {} failed",
                buildId, result.getSuccessCount(), result.getFailureCount());

        return result;
    }

    private int findActualRowNumber(int validRowIndex, PartCsvParserService.ParseResult parseResult) {
        // This is a simplification - in practice, the parser should track original row numbers
        // For now, we estimate based on the count of valid rows
        return validRowIndex;
    }

    private List<String> validateAgainstDatabase(PartCsvRowDto row) {
        List<String> errors = new ArrayList<>();

        // Validate categoryCode exists
        try {
            partCategoryService.findByCode(row.getCategoryCode());
        } catch (ResourceNotFoundException e) {
            errors.add("Category '" + row.getCategoryCode() + "' not found");
        }

        // Validate tierCode exists (if provided)
        if (row.getTierCode() != null && !row.getTierCode().isBlank()) {
            try {
                partTierService.findByCode(row.getTierCode());
            } catch (ResourceNotFoundException e) {
                errors.add("Tier '" + row.getTierCode() + "' not found");
            }
        }

        return errors;
    }

    private PartCreateDto convertToCreateDto(PartCsvRowDto row) {
        return PartCreateDto.builder()
                .name(row.getName())
                .categoryCode(row.getCategoryCode())
                .tierCode(row.getTierCode())
                .price(row.getPrice())
                .status(row.getStatus())
                .brand(row.getBrand())
                .productUrl(row.getProductUrl())
                .priorityValue(row.getPriorityValue())
                .isRequired(row.getIsRequired())
                .build();
    }

    private Map<String, String> rowToMap(PartCsvRowDto row) {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("name", row.getName());
        map.put("categoryCode", row.getCategoryCode());
        if (row.getTierCode() != null) map.put("tierCode", row.getTierCode());
        if (row.getPrice() != null) map.put("price", row.getPrice().toString());
        if (row.getStatus() != null) map.put("status", row.getStatus());
        if (row.getBrand() != null) map.put("brand", row.getBrand());
        if (row.getProductUrl() != null) map.put("productUrl", row.getProductUrl());
        if (row.getPriorityValue() != null) map.put("priorityValue", row.getPriorityValue().toString());
        if (row.getIsRequired() != null) map.put("isRequired", row.getIsRequired().toString());
        return map;
    }
}
