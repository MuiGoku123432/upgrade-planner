package com.sentinovo.carbuildervin.mcp.tools;

import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.service.parts.PartCategoryService;
import com.sentinovo.carbuildervin.service.parts.PartTierService;
import com.sentinovo.carbuildervin.service.vehicle.UpgradeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tools for lookup operations (categories, tiers).
 * These are reference data available to all authenticated users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookupMcpTools {

    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;
    private final UpgradeCategoryService upgradeCategoryService;

    @McpTool(name = "listPartCategories",
            description = "List all available part categories (e.g., SUSPENSION, ARMOR, WHEELS_TIRES)",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public List<PartCategoryDto> listPartCategories() {
        log.info("MCP: Listing part categories");
        return partCategoryService.getAllPartCategories();
    }

    @McpTool(name = "listPartTiers",
            description = "List all available part tiers (quality/price levels like BUDGET, MID, PREMIUM)",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public List<PartTierDto> listPartTiers() {
        log.info("MCP: Listing part tiers");
        return partTierService.getAllPartTiers();
    }

    @McpTool(name = "listUpgradeCategories",
            description = "List all available upgrade categories for builds (e.g., SUSPENSION, ENGINE, EXTERIOR)",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public List<UpgradeCategoryDto> listUpgradeCategories() {
        log.info("MCP: Listing upgrade categories");
        return upgradeCategoryService.getAllUpgradeCategories();
    }
}
