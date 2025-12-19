package com.sentinovo.carbuildervin.mcp.tools;

import com.sentinovo.carbuildervin.service.external.VinDecodingService;
import com.sentinovo.carbuildervin.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP Tools for VIN decoding operations.
 * These tools are available to all authenticated users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VinDecodeMcpTools {

    private final VinDecodingService vinDecodingService;

    @McpTool(name = "decodeVin",
            description = "Decode a VIN (Vehicle Identification Number) to get vehicle details like year, make, model, and trim",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String decodeVin(
            @McpToolParam(description = "The 17-character VIN to decode") String vin
    ) {
        log.info("MCP: Decoding VIN: {}", vin);

        if (vin == null || vin.isBlank()) {
            return "Error: VIN is required";
        }

        String cleanVin = vin.trim().toUpperCase();

        try {
            ValidationUtils.validateVin(cleanVin);
        } catch (Exception e) {
            return "Error: Invalid VIN format. VIN must be exactly 17 characters and not contain I, O, or Q.";
        }

        try {
            VinDecodingService.VinDecodingResponse decoded = vinDecodingService.decodeVin(cleanVin);

            return String.format(
                "VIN Decode Results for: %s\n" +
                "- Year: %d\n" +
                "- Make: %s\n" +
                "- Model: %s\n" +
                "- Trim: %s",
                cleanVin,
                decoded.getYear(),
                decoded.getMake() != null ? decoded.getMake() : "Unknown",
                decoded.getModel() != null ? decoded.getModel() : "Unknown",
                decoded.getTrim() != null ? decoded.getTrim() : "N/A"
            );
        } catch (Exception e) {
            log.error("VIN decode failed for {}: {}", cleanVin, e.getMessage());
            return "Error decoding VIN: " + e.getMessage();
        }
    }

    @McpTool(name = "validateVin",
            description = "Validate if a VIN is correctly formatted (17 characters, no I/O/Q)",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String validateVin(
            @McpToolParam(description = "The VIN to validate") String vin
    ) {
        log.info("MCP: Validating VIN: {}", vin);

        if (vin == null || vin.isBlank()) {
            return "Invalid: VIN is required";
        }

        String cleanVin = vin.trim().toUpperCase();

        try {
            ValidationUtils.validateVin(cleanVin);
            return String.format("Valid: '%s' is a correctly formatted VIN", cleanVin);
        } catch (Exception e) {
            return String.format("Invalid: '%s' - %s", cleanVin, e.getMessage());
        }
    }
}
