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
@Schema(description = "Response from VIN decoding service")
public class VinDecodeResponseDto {

    @Schema(description = "Original VIN", example = "JTEVA5AR9S5004482")
    private String vin;

    @Schema(description = "Whether VIN is valid", example = "true")
    private Boolean isValid;

    @Schema(description = "Vehicle year", example = "2021")
    private Integer year;

    @Schema(description = "Vehicle make", example = "Toyota")
    private String make;

    @Schema(description = "Vehicle model", example = "4Runner")
    private String model;

    @Schema(description = "Vehicle trim level", example = "TRD Off Road")
    private String trim;

    @Schema(description = "Body type", example = "SUV")
    private String bodyType;

    @Schema(description = "Vehicle type", example = "Truck")
    private String vehicleType;

    @Schema(description = "Transmission type", example = "Automatic")
    private String transmission;

    @Schema(description = "Drivetrain configuration", example = "4WD")
    private String drivetrain;

    @Schema(description = "Fuel type", example = "Gasoline")
    private String fuelType;

    @Schema(description = "Engine description", example = "4.0L V6")
    private String engine;

    @Schema(description = "Engine displacement in liters", example = "4.0")
    private Double engineSize;

    @Schema(description = "Number of doors", example = "4")
    private Integer doors;

    @Schema(description = "Number of cylinders", example = "6")
    private Integer cylinders;

    @Schema(description = "Country of manufacture", example = "Japan")
    private String madeIn;

    @Schema(description = "Whether decode was successful", example = "true")
    private Boolean success;
}