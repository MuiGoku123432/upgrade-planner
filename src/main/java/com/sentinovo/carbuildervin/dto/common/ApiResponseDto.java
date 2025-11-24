package com.sentinovo.carbuildervin.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponseDto<T> {

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Success status", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Error details when success is false")
    private ErrorResponseDto error;

    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder()
                .data(data)
                .success(true)
                .message("Success")
                .build();
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .data(data)
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponseDto<T> error(ErrorResponseDto error) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}