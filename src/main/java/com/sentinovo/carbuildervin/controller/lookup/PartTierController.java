package com.sentinovo.carbuildervin.controller.lookup;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierDto;
import com.sentinovo.carbuildervin.service.parts.PartTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/part-tiers")
@RequiredArgsConstructor
@Tag(name = "Part Tiers", description = "Part tier lookup operations")
public class PartTierController extends BaseController {

    private final PartTierService partTierService;

    @Operation(
        summary = "List part tiers", 
        description = "Get all available part tiers ordered by rank"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Part tiers retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))
    )
    @GetMapping
    public ResponseEntity<StandardApiResponse<List<PartTierDto>>> getPartTiers() {
        log.info("Getting all part tiers");
        
        List<PartTierDto> tiers = partTierService.getAllPartTiers();
        
        log.info("Retrieved {} part tiers", tiers.size());
        return success(tiers);
    }

    @Operation(
        summary = "Get part tier by code", 
        description = "Get a specific part tier by its code"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Part tier retrieved successfully",
        content = @Content(schema = @Schema(implementation = PartTierDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Part tier not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/{code}")
    public ResponseEntity<StandardApiResponse<PartTierDto>> getPartTier(@PathVariable String code) {
        log.info("Getting part tier with code: {}", code);
        
        PartTierDto tier = partTierService.getPartTierByCode(code);
        
        return success(tier);
    }
}