package com.sentinovo.carbuildervin.dto.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new vehicle")
public class VehicleCreateDto {

    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must be 17 characters and contain valid characters")
    @Schema(description = "Vehicle Identification Number (optional)", example = "JTEVA5AR9S5004482")
    private String vin;

    @Min(value = 1886, message = "Year cannot be before 1886")
    @Max(value = 2030, message = "Year cannot be after 2030")
    @Schema(description = "Vehicle year", example = "2021")
    private Integer year;

    @Size(max = 100, message = "Make cannot exceed 100 characters")
    @Schema(description = "Vehicle make", example = "Toyota")
    private String make;

    @Size(max = 100, message = "Model cannot exceed 100 characters")
    @Schema(description = "Vehicle model", example = "4Runner")
    private String model;

    @Size(max = 100, message = "Trim cannot exceed 100 characters")
    @Schema(description = "Vehicle trim level", example = "TRD Off Road")
    private String trim;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "User-defined nickname", example = "Trail Rig")
    private String nickname;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Going full overland build")
    private String notes;
}