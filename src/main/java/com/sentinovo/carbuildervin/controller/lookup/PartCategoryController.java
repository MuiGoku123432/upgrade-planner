package com.sentinovo.carbuildervin.controller.lookup;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryDto;
import com.sentinovo.carbuildervin.service.parts.PartCategoryService;
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
@RequestMapping("/api/v1/part-categories")
@RequiredArgsConstructor
@Tag(name = "Part Categories", description = "Part category lookup operations")
public class PartCategoryController extends BaseController {

    private final PartCategoryService partCategoryService;

    @Operation(
        summary = "List part categories", 
        description = "Get all available part categories"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Part categories retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))
    )
    @GetMapping
    public ResponseEntity<StandardApiResponse<List<PartCategoryDto>>> getPartCategories() {
        log.info("Getting all part categories");
        
        List<PartCategoryDto> categories = partCategoryService.getAllPartCategories();
        
        log.info("Retrieved {} part categories", categories.size());
        return success(categories);
    }

    @Operation(
        summary = "Get part category by code", 
        description = "Get a specific part category by its code"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Part category retrieved successfully",
        content = @Content(schema = @Schema(implementation = PartCategoryDto.class))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Part category not found",
        content = @Content(schema = @Schema(implementation = StandardApiResponse.class))
    )
    @GetMapping("/{code}")
    public ResponseEntity<StandardApiResponse<PartCategoryDto>> getPartCategory(@PathVariable String code) {
        log.info("Getting part category with code: {}", code);
        
        PartCategoryDto category = partCategoryService.getPartCategoryByCode(code);
        
        return success(category);
    }
}