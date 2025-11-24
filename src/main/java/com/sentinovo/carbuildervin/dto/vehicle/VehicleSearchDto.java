package com.sentinovo.carbuildervin.dto.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search criteria for vehicles")
public class VehicleSearchDto {

    @Schema(description = "Exact VIN match", example = "JTEVA5AR9S5004482")
    private String vin;

    @Schema(description = "Vehicle make", example = "Toyota")
    private String make;

    @Schema(description = "Vehicle model", example = "4Runner")
    private String model;

    @Min(value = 1886, message = "Year cannot be before 1886")
    @Max(value = 2030, message = "Year cannot be after 2030")
    @Schema(description = "Vehicle year", example = "2021")
    private Integer year;

    @Schema(description = "Vehicle trim level", example = "TRD")
    private String trim;

    @Schema(description = "User-defined nickname", example = "Trail")
    private String nickname;

    @Schema(description = "Include archived vehicles", example = "false")
    private Boolean includeArchived;

    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "20")
    private Integer size;

    @Schema(description = "Sort field", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection;
}