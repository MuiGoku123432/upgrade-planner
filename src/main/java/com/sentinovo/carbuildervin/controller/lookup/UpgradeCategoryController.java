package com.sentinovo.carbuildervin.controller.lookup;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.service.vehicle.UpgradeCategoryService;
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
@RequestMapping("/api/v1/upgrade-categories")
@RequiredArgsConstructor
@Tag(name = "Upgrade Categories", description = "Upgrade category lookup operations")
public class UpgradeCategoryController extends BaseController {

    private final UpgradeCategoryService upgradeCategoryService;

    @Operation(
        summary = "List upgrade categories", 
        description = "Get all available upgrade categories"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Upgrade categories retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))
    )
    @GetMapping
    public ResponseEntity<StandardApiResponse<List<UpgradeCategoryDto>>> getUpgradeCategories() {
        log.info("Getting all upgrade categories");
        
        List<UpgradeCategoryDto> categories = upgradeCategoryService.getAllUpgradeCategories();
        
        log.info("Retrieved {} upgrade categories", categories.size());
        return success(categories);
    }

    @Operation(
        summary = "Get upgrade category by ID", 
        description = "Get a specific upgrade category by its ID"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Upgrade category retrieved successfully",
        content = @Content(schema = @Schema(implementation = UpgradeCategoryDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Upgrade category not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/{id}")
    public ResponseEntity<StandardApiResponse<UpgradeCategoryDto>> getUpgradeCategory(@PathVariable Integer id) {
        log.info("Getting upgrade category with ID: {}", id);
        
        UpgradeCategoryDto category = upgradeCategoryService.getUpgradeCategoryById(id);
        
        return success(category);
    }

    @Operation(
        summary = "Get upgrade category by key", 
        description = "Get a specific upgrade category by its key"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Upgrade category retrieved successfully",
        content = @Content(schema = @Schema(implementation = UpgradeCategoryDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Upgrade category not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/key/{key}")
    public ResponseEntity<StandardApiResponse<UpgradeCategoryDto>> getUpgradeCategoryByKey(@PathVariable String key) {
        log.info("Getting upgrade category with key: {}", key);
        
        UpgradeCategoryDto category = upgradeCategoryService.getUpgradeCategoryByName(key);
        
        return success(category);
    }

    @Operation(
        summary = "List active upgrade categories", 
        description = "Get all active upgrade categories"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Active upgrade categories retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))
    )
    @GetMapping("/active")
    public ResponseEntity<StandardApiResponse<List<UpgradeCategoryDto>>> getActiveUpgradeCategories() {
        log.info("Getting active upgrade categories");
        
        List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();
        
        log.info("Retrieved {} active upgrade categories", categories.size());
        return success(categories);
    }
}