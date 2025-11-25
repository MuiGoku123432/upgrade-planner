package com.sentinovo.carbuildervin.controller.vehicle;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.vin.VinDecodeRequestDto;
import com.sentinovo.carbuildervin.dto.vin.VinDecodeResponseDto;
import com.sentinovo.carbuildervin.service.external.VinDecodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/vin")
@RequiredArgsConstructor
@Tag(name = "VIN Decoding", description = "VIN decoding using external service")
public class VinController extends BaseController {

    private final VinDecodingService vinDecodingService;

    @Operation(
        summary = "Decode VIN", 
        description = "Decode a VIN using the MarketCheck external service to get vehicle details"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "VIN decoded successfully",
        content = @Content(schema = @Schema(implementation = VinDecodeResponseDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "Invalid VIN format",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422", 
        description = "VIN cannot be decoded",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "503", 
        description = "External service unavailable",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @PostMapping("/decode")
    public ResponseEntity<StandardApiResponse<VinDecodeResponseDto>> decodeVin(@Valid @RequestBody VinDecodeRequestDto request) {
        log.info("VIN decode request for VIN: {}", request.getVin());
        
        VinDecodingService.VinDecodingResponse serviceResult = vinDecodingService.decodeVin(request.getVin());
        VinDecodeResponseDto result = convertToDto(serviceResult);
        
        log.info("VIN decoded successfully: {}", request.getVin());
        return success(result, "VIN decoded successfully");
    }

    @Operation(
        summary = "Decode VIN (GET)", 
        description = "Alternative GET endpoint to decode a VIN using path parameter"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "VIN decoded successfully",
        content = @Content(schema = @Schema(implementation = VinDecodeResponseDto.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", 
        description = "Invalid VIN format",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422", 
        description = "VIN cannot be decoded",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "503", 
        description = "External service unavailable",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/decode/{vin}")
    public ResponseEntity<StandardApiResponse<VinDecodeResponseDto>> decodeVinGet(@PathVariable String vin) {
        log.info("VIN decode request (GET) for VIN: {}", vin);
        
        if (vin == null || vin.trim().isEmpty()) {
            log.warn("Empty VIN provided");
            return ResponseEntity.badRequest().body(
                    StandardApiResponse.<VinDecodeResponseDto>builder()
                            .success(false)
                            .message("VIN cannot be empty")
                            .build()
            );
        }
        
        VinDecodingService.VinDecodingResponse serviceResult = vinDecodingService.decodeVin(vin.trim().toUpperCase());
        VinDecodeResponseDto result = convertToDto(serviceResult);
        
        log.info("VIN decoded successfully (GET): {}", vin);
        return success(result, "VIN decoded successfully");
    }
    
    private VinDecodeResponseDto convertToDto(VinDecodingService.VinDecodingResponse serviceResponse) {
        return VinDecodeResponseDto.builder()
                .vin(serviceResponse.getVin())
                .isValid(serviceResponse.isSuccess())
                .year(serviceResponse.getYear())
                .make(serviceResponse.getMake())
                .model(serviceResponse.getModel())
                .trim(serviceResponse.getTrim())
                .bodyType(serviceResponse.getBodyType())
                .transmission(serviceResponse.getTransmission())
                .drivetrain(serviceResponse.getDrivetrain())
                .fuelType(serviceResponse.getFuelType())
                .engine(serviceResponse.getEngine())
                .success(serviceResponse.isSuccess())
                .build();
    }
}