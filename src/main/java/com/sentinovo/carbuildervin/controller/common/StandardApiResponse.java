package com.sentinovo.carbuildervin.controller.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class StandardApiResponse<T> {

    @Schema(description = "Whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error information")
    private ErrorInfo error;

    @Schema(description = "Response timestamp", example = "2025-11-24T15:30:00Z")
    private OffsetDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error information")
    public static class ErrorInfo {
        
        @Schema(description = "Error code", example = "RESOURCE_NOT_FOUND")
        private String code;
        
        @Schema(description = "Error message", example = "Vehicle not found")
        private String message;
        
        @Schema(description = "Additional error details")
        private Object details;
    }

    public static <T> StandardApiResponse<T> success(T data) {
        return StandardApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> StandardApiResponse<T> success(T data, String message) {
        return StandardApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> StandardApiResponse<T> error(String message) {
        return StandardApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .message(message)
                        .build())
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> StandardApiResponse<T> error(String code, String message) {
        return StandardApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> StandardApiResponse<T> error(String code, String message, Object details) {
        return StandardApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(OffsetDateTime.now())
                .build();
    }
}