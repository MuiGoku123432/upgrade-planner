package com.sentinovo.carbuildervin.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponseDto {

    @Schema(description = "Error code", example = "RESOURCE_NOT_FOUND")
    private String code;

    @Schema(description = "Human-readable error message", example = "Vehicle not found")
    private String message;

    @Schema(description = "Additional error details")
    private Map<String, Object> details;

    @Schema(description = "Timestamp when error occurred", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime timestamp;

    @Schema(description = "Request path that caused the error", example = "/api/v1/vehicles/123")
    private String path;
}