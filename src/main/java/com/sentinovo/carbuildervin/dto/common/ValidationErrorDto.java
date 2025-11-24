package com.sentinovo.carbuildervin.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation error details")
public class ValidationErrorDto {

    @Schema(description = "Field validation errors - field name to error message")
    private Map<String, String> fieldErrors;

    @Schema(description = "Global validation errors")
    private String globalError;

    @Schema(description = "Object name that failed validation", example = "createVehicleRequest")
    private String objectName;
}