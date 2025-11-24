package com.sentinovo.carbuildervin.dto.vin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "VIN validation result")
public class VinValidationResponseDto {

    @Schema(description = "Original VIN", example = "JTEVA5AR9S5004482")
    private String vin;

    @Schema(description = "Whether VIN format is valid", example = "true")
    private Boolean isValid;

    @Schema(description = "Validation error message if invalid", example = "Invalid check digit")
    private String errorMessage;

    @Schema(description = "Whether VIN can be decoded", example = "true")
    private Boolean canDecode;
}