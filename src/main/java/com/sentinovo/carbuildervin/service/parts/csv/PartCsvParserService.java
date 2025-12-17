package com.sentinovo.carbuildervin.service.parts.csv;

import com.sentinovo.carbuildervin.dto.parts.csv.CsvRowErrorDto;
import com.sentinovo.carbuildervin.dto.parts.csv.PartCsvRowDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for parsing and validating CSV files containing part data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartCsvParserService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "PLANNED", "ORDERED", "SHIPPED", "DELIVERED", "INSTALLED", "CANCELLED"
    );

    private static final Set<String> REQUIRED_HEADERS = Set.of("name", "categoryCode");

    private static final Set<String> ALL_HEADERS = Set.of(
            "name", "categoryCode", "tierCode", "price", "status",
            "brand", "productUrl", "priorityValue", "isRequired"
    );

    /**
     * Parse and validate a CSV file.
     *
     * @param file the uploaded CSV file
     * @return ParseResult containing valid rows and errors
     */
    public ParseResult parseAndValidate(MultipartFile file) {
        List<PartCsvRowDto> validRows = new ArrayList<>();
        List<CsvRowErrorDto> errors = new ArrayList<>();
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

            try (CSVParser parser = new CSVParser(reader, format)) {
                // Validate headers
                List<String> headerErrors = validateHeaders(parser.getHeaderNames());
                if (!headerErrors.isEmpty()) {
                    errors.add(CsvRowErrorDto.builder()
                            .rowNumber(0)
                            .rowData(Map.of("headers", String.join(", ", parser.getHeaderNames())))
                            .errors(headerErrors)
                            .build());
                    return new ParseResult(validRows, errors, 0);
                }

                for (CSVRecord record : parser) {
                    totalRows++;
                    int rowNumber = (int) record.getRecordNumber();

                    Map<String, String> rowData = recordToMap(record);
                    List<String> rowErrors = validateRow(rowData, rowNumber);

                    if (rowErrors.isEmpty()) {
                        PartCsvRowDto dto = parseRow(rowData);
                        validRows.add(dto);
                    } else {
                        errors.add(CsvRowErrorDto.builder()
                                .rowNumber(rowNumber)
                                .rowData(rowData)
                                .errors(rowErrors)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing CSV file", e);
            errors.add(CsvRowErrorDto.builder()
                    .rowNumber(0)
                    .rowData(Map.of())
                    .errors(List.of("Failed to parse CSV file: " + e.getMessage()))
                    .build());
        }

        log.info("CSV parsing complete: {} total rows, {} valid, {} errors",
                totalRows, validRows.size(), errors.size());

        return new ParseResult(validRows, errors, totalRows);
    }

    private List<String> validateHeaders(List<String> headers) {
        List<String> errors = new ArrayList<>();

        // Check for required headers
        for (String required : REQUIRED_HEADERS) {
            if (!headers.contains(required)) {
                errors.add("Missing required header: " + required);
            }
        }

        // Warn about unknown headers (but don't fail)
        for (String header : headers) {
            if (!ALL_HEADERS.contains(header)) {
                log.warn("Unknown header in CSV: {}", header);
            }
        }

        return errors;
    }

    private Map<String, String> recordToMap(CSVRecord record) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String header : record.getParser().getHeaderNames()) {
            if (record.isMapped(header)) {
                String value = record.get(header);
                map.put(header, value != null ? value.trim() : "");
            }
        }
        return map;
    }

    private List<String> validateRow(Map<String, String> row, int rowNumber) {
        List<String> errors = new ArrayList<>();

        // Required field: name
        String name = row.get("name");
        if (name == null || name.isBlank()) {
            errors.add("Name is required");
        } else if (name.length() > 200) {
            errors.add("Name exceeds 200 characters");
        }

        // Required field: categoryCode
        String categoryCode = row.get("categoryCode");
        if (categoryCode == null || categoryCode.isBlank()) {
            errors.add("Category code is required");
        }

        // Optional field: price
        String price = row.get("price");
        if (price != null && !price.isBlank()) {
            try {
                BigDecimal priceValue = new BigDecimal(price);
                if (priceValue.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("Price must be non-negative");
                }
            } catch (NumberFormatException e) {
                errors.add("Price must be a valid number");
            }
        }

        // Optional field: status
        String status = row.get("status");
        if (status != null && !status.isBlank()) {
            if (!VALID_STATUSES.contains(status.toUpperCase())) {
                errors.add("Invalid status '" + status + "'. Valid values: " + String.join(", ", VALID_STATUSES));
            }
        }

        // Optional field: priorityValue
        String priorityValue = row.get("priorityValue");
        if (priorityValue != null && !priorityValue.isBlank()) {
            try {
                int priority = Integer.parseInt(priorityValue);
                if (priority < 1 || priority > 1000) {
                    errors.add("Priority value must be between 1 and 1000");
                }
            } catch (NumberFormatException e) {
                errors.add("Priority value must be a valid integer");
            }
        }

        // Optional field: productUrl
        String productUrl = row.get("productUrl");
        if (productUrl != null && !productUrl.isBlank()) {
            if (!productUrl.startsWith("http://") && !productUrl.startsWith("https://")) {
                errors.add("Product URL must start with http:// or https://");
            }
        }

        // Optional field: isRequired
        String isRequired = row.get("isRequired");
        if (isRequired != null && !isRequired.isBlank()) {
            String lower = isRequired.toLowerCase();
            if (!Set.of("true", "false", "yes", "no", "1", "0").contains(lower)) {
                errors.add("isRequired must be true, false, yes, no, 1, or 0");
            }
        }

        // Optional field: brand
        String brand = row.get("brand");
        if (brand != null && brand.length() > 100) {
            errors.add("Brand exceeds 100 characters");
        }

        return errors;
    }

    private PartCsvRowDto parseRow(Map<String, String> row) {
        return PartCsvRowDto.builder()
                .name(row.get("name"))
                .categoryCode(row.get("categoryCode"))
                .tierCode(getOrNull(row, "tierCode"))
                .price(parsePrice(row.get("price")))
                .status(parseStatus(row.get("status")))
                .brand(getOrNull(row, "brand"))
                .productUrl(getOrNull(row, "productUrl"))
                .priorityValue(parsePriority(row.get("priorityValue")))
                .isRequired(parseBoolean(row.get("isRequired")))
                .build();
    }

    private String getOrNull(Map<String, String> row, String key) {
        String value = row.get(key);
        return (value != null && !value.isBlank()) ? value : null;
    }

    private BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toUpperCase();
    }

    private Integer parsePriority(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String lower = value.toLowerCase();
        if (Set.of("true", "yes", "1").contains(lower)) {
            return true;
        } else if (Set.of("false", "no", "0").contains(lower)) {
            return false;
        }
        return null;
    }

    /**
     * Result of parsing a CSV file.
     */
    public record ParseResult(
            List<PartCsvRowDto> validRows,
            List<CsvRowErrorDto> errors,
            int totalRows
    ) {}
}
