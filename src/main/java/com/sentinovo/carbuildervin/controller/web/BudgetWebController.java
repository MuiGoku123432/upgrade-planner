package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.dto.budget.*;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.entities.parts.SubPart;
import com.sentinovo.carbuildervin.service.parts.PartCategoryService;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.parts.PartTierService;
import com.sentinovo.carbuildervin.service.parts.SubPartService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.vehicle.UpgradeCategoryService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web controller for budget/numbers page - cost analysis and budget tracking
 */
@Controller
@RequestMapping("/budget")
@RequiredArgsConstructor
@Slf4j
public class BudgetWebController {

    private final VehicleService vehicleService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final PartService partService;
    private final SubPartService subPartService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;
    private final UpgradeCategoryService upgradeCategoryService;
    private final AuthenticationService authenticationService;

    private static final List<String> PART_STATUSES = Arrays.asList(
            "PLANNED", "RESEARCHING", "ORDERED", "DELIVERED", "INSTALLED", "CANCELLED"
    );

    private static final List<String> BUILD_STATUSES = Arrays.asList(
            "PLANNED", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"
    );

    // ==================== GLOBAL BUDGET OVERVIEW ====================

    /**
     * Global budget overview page
     */
    @GetMapping
    public String budgetOverviewPage(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading budget overview page for user: {}", currentUserId);

        // Get filter dropdown data (same as status page)
        List<VehicleDto> userVehicles = vehicleService.getUserVehicles(currentUserId);
        List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();

        model.addAttribute("vehicles", userVehicles);
        model.addAttribute("categories", categories);
        model.addAttribute("buildStatuses", BUILD_STATUSES);

        return "budget/index";
    }

    /**
     * HTMX fragment - filtered build budget overview cards
     */
    @GetMapping("/fragment/overview")
    public String getOverviewFragment(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) String categoryKey,
            @RequestParam(required = false) String buildStatus,
            Model model) {

        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading budget overview fragment for user: {}, vehicleId: {}, categoryKey: {}, buildStatus: {}",
                currentUserId, vehicleId, categoryKey, buildStatus);

        List<BudgetOverviewDto> budgetOverviews = calculateBudgetOverviews(
                currentUserId, vehicleId, categoryKey, buildStatus);

        // Calculate grand total from all build overviews
        BigDecimal grandTotal = budgetOverviews.stream()
                .map(o -> o.getTotalCost() != null ? o.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("budgetOverviews", budgetOverviews);
        model.addAttribute("grandTotal", grandTotal);

        return "budget/fragments/overview :: overview";
    }

    // ==================== BUILD-SPECIFIC BUDGET ====================

    /**
     * Build budget page
     */
    @GetMapping("/build/{buildId}")
    public String budgetPage(@PathVariable UUID buildId, Model model) {
        log.debug("Loading budget page for build: {}", buildId);

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        VehicleDto vehicle = vehicleService.getVehicleById(build.getVehicleId());

        // Load filter dropdown data
        List<PartCategoryDto> categories = partCategoryService.getAllPartCategories();
        List<PartTierDto> tiers = partTierService.getAllPartTiers();

        model.addAttribute("build", build);
        model.addAttribute("vehicleLabel", getVehicleLabel(vehicle));
        model.addAttribute("categories", categories);
        model.addAttribute("tiers", tiers);
        model.addAttribute("statuses", PART_STATUSES);

        return "budget/build";
    }

    /**
     * HTMX fragment - build budget calculation results
     */
    @GetMapping("/build/{buildId}/fragment/results")
    public String getResultsFragment(
            @PathVariable UUID buildId,
            @RequestParam(defaultValue = "true") boolean includeRequired,
            @RequestParam(defaultValue = "true") boolean includeOptional,
            @RequestParam(required = false) List<String> categoryCodes,
            @RequestParam(required = false) List<String> tierCodes,
            @RequestParam(required = false) Integer minPriority,
            @RequestParam(required = false) Integer maxPriority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> statuses,
            Model model) {

        log.debug("Loading budget results fragment for build: {}", buildId);

        BudgetCalcResponseDto result = calculateBuildBudget(buildId,
                includeRequired, includeOptional, categoryCodes, tierCodes,
                minPriority, maxPriority, startDate, endDate, statuses);

        model.addAttribute("result", result);
        model.addAttribute("buildId", buildId);

        return "budget/fragments/results :: results";
    }

    /**
     * HTMX fragment - detailed line items table
     */
    @GetMapping("/build/{buildId}/fragment/line-items")
    public String getLineItemsFragment(
            @PathVariable UUID buildId,
            @RequestParam(defaultValue = "true") boolean includeRequired,
            @RequestParam(defaultValue = "true") boolean includeOptional,
            @RequestParam(required = false) List<String> categoryCodes,
            @RequestParam(required = false) List<String> tierCodes,
            @RequestParam(required = false) Integer minPriority,
            @RequestParam(required = false) Integer maxPriority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> statuses,
            Model model) {

        log.debug("Loading line items fragment for build: {}", buildId);

        List<BudgetLineItemDto> lineItems = getFilteredLineItems(buildId,
                includeRequired, includeOptional, categoryCodes, tierCodes,
                minPriority, maxPriority, startDate, endDate, statuses);

        model.addAttribute("lineItems", lineItems);

        return "budget/fragments/line-items :: line-items";
    }

    // ==================== Calculation Logic ====================

    private List<BudgetOverviewDto> calculateBudgetOverviews(
            UUID userId, UUID vehicleId, String categoryKey, String buildStatus) {

        // Get all user's vehicles
        List<VehicleDto> userVehicles = vehicleService.getUserVehicles(userId);

        // Filter by vehicle if specified
        if (vehicleId != null) {
            userVehicles = userVehicles.stream()
                    .filter(v -> v.getId().equals(vehicleId))
                    .collect(Collectors.toList());
        }

        List<BudgetOverviewDto> overviews = new ArrayList<>();

        for (VehicleDto vehicle : userVehicles) {
            List<VehicleUpgradeDto> builds = vehicleUpgradeService.getVehicleUpgradesByVehicleId(vehicle.getId());

            // Filter by category if specified
            if (categoryKey != null && !categoryKey.isEmpty()) {
                builds = builds.stream()
                        .filter(b -> categoryKey.equals(getCategoryKey(b.getUpgradeCategoryId())))
                        .collect(Collectors.toList());
            }

            // Filter by build status if specified
            if (buildStatus != null && !buildStatus.isEmpty()) {
                builds = builds.stream()
                        .filter(b -> buildStatus.equals(b.getStatus()))
                        .collect(Collectors.toList());
            }

            for (VehicleUpgradeDto build : builds) {
                BudgetOverviewDto overview = calculateSingleBuildOverview(build, vehicle);
                overviews.add(overview);
            }
        }

        // Sort by priority level, then by name
        overviews.sort(Comparator
                .comparing(BudgetOverviewDto::getPriorityLevel, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(BudgetOverviewDto::getBuildName));

        return overviews;
    }

    private BudgetOverviewDto calculateSingleBuildOverview(VehicleUpgradeDto build, VehicleDto vehicle) {
        List<BudgetLineItemDto> allItems = collectAllLineItems(build.getId());

        BigDecimal requiredCost = allItems.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsRequired()))
                .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal optionalCost = allItems.stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsRequired()))
                .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long requiredCount = allItems.stream().filter(i -> Boolean.TRUE.equals(i.getIsRequired())).count();
        long optionalCount = allItems.stream().filter(i -> !Boolean.TRUE.equals(i.getIsRequired())).count();

        long requiredInstalled = allItems.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsRequired()) && "INSTALLED".equals(i.getStatus()))
                .count();

        double percentInstalled = requiredCount == 0 ? 0 : (requiredInstalled * 100.0 / requiredCount);

        return BudgetOverviewDto.builder()
                .buildId(build.getId())
                .vehicleId(vehicle.getId())
                .vehicleLabel(getVehicleLabel(vehicle))
                .buildName(build.getName())
                .upgradeCategoryName(build.getUpgradeCategoryName())
                .status(build.getStatus())
                .priorityLevel(build.getPriorityLevel())
                .totalCost(requiredCost.add(optionalCost))
                .requiredCost(requiredCost)
                .optionalCost(optionalCost)
                .totalItemsCount(allItems.size())
                .requiredItemsCount(requiredCount)
                .optionalItemsCount(optionalCount)
                .percentRequiredInstalled(Math.round(percentInstalled * 10.0) / 10.0)
                .build();
    }

    private BudgetCalcResponseDto calculateBuildBudget(
            UUID buildId,
            boolean includeRequired, boolean includeOptional,
            List<String> categoryCodes, List<String> tierCodes,
            Integer minPriority, Integer maxPriority,
            LocalDate startDate, LocalDate endDate,
            List<String> statuses) {

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        VehicleDto vehicle = vehicleService.getVehicleById(build.getVehicleId());

        // Collect all line items (parts + sub-parts)
        List<BudgetLineItemDto> allItems = collectAllLineItems(buildId);

        // Apply filters
        List<BudgetLineItemDto> filteredItems = filterItems(allItems,
                includeRequired, includeOptional, categoryCodes, tierCodes,
                minPriority, maxPriority, startDate, endDate, statuses);

        // Calculate totals
        BigDecimal requiredCost = filteredItems.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsRequired()))
                .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal optionalCost = filteredItems.stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsRequired()))
                .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long requiredCount = filteredItems.stream().filter(i -> Boolean.TRUE.equals(i.getIsRequired())).count();
        long optionalCount = filteredItems.stream().filter(i -> !Boolean.TRUE.equals(i.getIsRequired())).count();

        // Group by category
        List<CategoryCostDto> byCategory = calculateByCategory(filteredItems);

        // Group by tier
        List<TierCostDto> byTier = calculateByTier(filteredItems);

        // Group by month
        List<MonthlyCostDto> byMonth = calculateByMonth(filteredItems);

        // Build filters DTO for display
        BudgetFiltersDto filters = BudgetFiltersDto.builder()
                .includeRequired(includeRequired)
                .includeOptional(includeOptional)
                .categoryCodes(categoryCodes)
                .tierCodes(tierCodes)
                .minPriority(minPriority)
                .maxPriority(maxPriority)
                .startDate(startDate)
                .endDate(endDate)
                .statuses(statuses)
                .build();

        return BudgetCalcResponseDto.builder()
                .buildId(buildId)
                .buildName(build.getName())
                .vehicleLabel(getVehicleLabel(vehicle))
                .currencyCode("USD")
                .filters(filters)
                .requiredCost(requiredCost)
                .optionalCost(optionalCost)
                .combinedCost(requiredCost.add(optionalCost))
                .byCategory(byCategory)
                .byTier(byTier)
                .byMonth(byMonth)
                .totalItemsCount(filteredItems.size())
                .requiredItemsCount(requiredCount)
                .optionalItemsCount(optionalCount)
                .build();
    }

    private List<BudgetLineItemDto> getFilteredLineItems(
            UUID buildId,
            boolean includeRequired, boolean includeOptional,
            List<String> categoryCodes, List<String> tierCodes,
            Integer minPriority, Integer maxPriority,
            LocalDate startDate, LocalDate endDate,
            List<String> statuses) {

        List<BudgetLineItemDto> allItems = collectAllLineItems(buildId);
        return filterItems(allItems, includeRequired, includeOptional,
                categoryCodes, tierCodes, minPriority, maxPriority,
                startDate, endDate, statuses);
    }

    private List<BudgetLineItemDto> collectAllLineItems(UUID buildId) {
        List<BudgetLineItemDto> items = new ArrayList<>();

        List<PartDto> parts = partService.getPartsByUpgradeId(buildId);

        for (PartDto part : parts) {
            // Add part as line item
            items.add(BudgetLineItemDto.builder()
                    .id(part.getId())
                    .type("PART")
                    .parentPartId(null)
                    .name(part.getName())
                    .brand(part.getBrand())
                    .categoryCode(part.getCategoryCode())
                    .categoryLabel(part.getCategoryName())
                    .tierCode(part.getTierCode())
                    .tierLabel(part.getTierName())
                    .isRequired(part.getIsRequired())
                    .priorityValue(part.getPriorityValue())
                    .targetPurchaseDate(part.getTargetPurchaseDate())
                    .status(part.getStatus())
                    .price(part.getPrice())
                    .currencyCode(part.getCurrencyCode())
                    .build());

            // Add sub-parts as line items
            List<SubPart> subParts = subPartService.findByParentPartId(part.getId());
            for (SubPart subPart : subParts) {
                items.add(BudgetLineItemDto.builder()
                        .id(subPart.getId())
                        .type("SUB_PART")
                        .parentPartId(part.getId())
                        .name(subPart.getName())
                        .brand(subPart.getBrand())
                        .categoryCode(subPart.getPartCategory() != null ? subPart.getPartCategory().getCode() : null)
                        .categoryLabel(subPart.getPartCategory() != null ? subPart.getPartCategory().getLabel() : null)
                        .tierCode(subPart.getPartTier() != null ? subPart.getPartTier().getCode() : null)
                        .tierLabel(subPart.getPartTier() != null ? subPart.getPartTier().getLabel() : null)
                        .isRequired(subPart.getIsRequired())
                        .priorityValue(subPart.getPriorityValue())
                        .targetPurchaseDate(subPart.getTargetPurchaseDate())
                        .status(subPart.getStatus())
                        .price(subPart.getPrice())
                        .currencyCode(subPart.getCurrencyCode())
                        .build());
            }
        }

        return items;
    }

    private List<BudgetLineItemDto> filterItems(
            List<BudgetLineItemDto> items,
            boolean includeRequired, boolean includeOptional,
            List<String> categoryCodes, List<String> tierCodes,
            Integer minPriority, Integer maxPriority,
            LocalDate startDate, LocalDate endDate,
            List<String> statuses) {

        return items.stream()
                .filter(item -> {
                    // Filter by required/optional
                    if (!includeRequired && Boolean.TRUE.equals(item.getIsRequired())) return false;
                    if (!includeOptional && !Boolean.TRUE.equals(item.getIsRequired())) return false;

                    // Filter by category
                    if (categoryCodes != null && !categoryCodes.isEmpty() &&
                            (item.getCategoryCode() == null || !categoryCodes.contains(item.getCategoryCode()))) {
                        return false;
                    }

                    // Filter by tier
                    if (tierCodes != null && !tierCodes.isEmpty() &&
                            (item.getTierCode() == null || !tierCodes.contains(item.getTierCode()))) {
                        return false;
                    }

                    // Filter by priority range
                    if (minPriority != null && item.getPriorityValue() != null &&
                            item.getPriorityValue() < minPriority) {
                        return false;
                    }
                    if (maxPriority != null && item.getPriorityValue() != null &&
                            item.getPriorityValue() > maxPriority) {
                        return false;
                    }

                    // Filter by date range
                    if (startDate != null && item.getTargetPurchaseDate() != null &&
                            item.getTargetPurchaseDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && item.getTargetPurchaseDate() != null &&
                            item.getTargetPurchaseDate().isAfter(endDate)) {
                        return false;
                    }

                    // Filter by status
                    if (statuses != null && !statuses.isEmpty() &&
                            (item.getStatus() == null || !statuses.contains(item.getStatus()))) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<CategoryCostDto> calculateByCategory(List<BudgetLineItemDto> items) {
        Map<String, List<BudgetLineItemDto>> grouped = items.stream()
                .filter(i -> i.getCategoryCode() != null)
                .collect(Collectors.groupingBy(BudgetLineItemDto::getCategoryCode));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<BudgetLineItemDto> categoryItems = entry.getValue();
                    String categoryLabel = categoryItems.stream()
                            .filter(i -> i.getCategoryLabel() != null)
                            .findFirst()
                            .map(BudgetLineItemDto::getCategoryLabel)
                            .orElse(entry.getKey());

                    BigDecimal requiredCost = categoryItems.stream()
                            .filter(i -> Boolean.TRUE.equals(i.getIsRequired()))
                            .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal optionalCost = categoryItems.stream()
                            .filter(i -> !Boolean.TRUE.equals(i.getIsRequired()))
                            .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return CategoryCostDto.builder()
                            .categoryCode(entry.getKey())
                            .categoryLabel(categoryLabel)
                            .requiredCost(requiredCost)
                            .optionalCost(optionalCost)
                            .combinedCost(requiredCost.add(optionalCost))
                            .itemCount(categoryItems.size())
                            .build();
                })
                .sorted(Comparator.comparing(CategoryCostDto::getCategoryLabel))
                .collect(Collectors.toList());
    }

    private List<TierCostDto> calculateByTier(List<BudgetLineItemDto> items) {
        Map<String, List<BudgetLineItemDto>> grouped = items.stream()
                .filter(i -> i.getTierCode() != null)
                .collect(Collectors.groupingBy(BudgetLineItemDto::getTierCode));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<BudgetLineItemDto> tierItems = entry.getValue();
                    String tierLabel = tierItems.stream()
                            .filter(i -> i.getTierLabel() != null)
                            .findFirst()
                            .map(BudgetLineItemDto::getTierLabel)
                            .orElse(entry.getKey());

                    BigDecimal cost = tierItems.stream()
                            .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return TierCostDto.builder()
                            .tierCode(entry.getKey())
                            .tierLabel(tierLabel)
                            .cost(cost)
                            .itemCount(tierItems.size())
                            .build();
                })
                .sorted(Comparator.comparing(TierCostDto::getTierLabel))
                .collect(Collectors.toList());
    }

    private List<MonthlyCostDto> calculateByMonth(List<BudgetLineItemDto> items) {
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<BudgetLineItemDto>> grouped = items.stream()
                .filter(i -> i.getTargetPurchaseDate() != null)
                .collect(Collectors.groupingBy(i -> i.getTargetPurchaseDate().format(monthFormatter)));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<BudgetLineItemDto> monthItems = entry.getValue();

                    BigDecimal requiredCost = monthItems.stream()
                            .filter(i -> Boolean.TRUE.equals(i.getIsRequired()))
                            .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal optionalCost = monthItems.stream()
                            .filter(i -> !Boolean.TRUE.equals(i.getIsRequired()))
                            .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return MonthlyCostDto.builder()
                            .yearMonth(entry.getKey())
                            .requiredCost(requiredCost)
                            .optionalCost(optionalCost)
                            .combinedCost(requiredCost.add(optionalCost))
                            .itemCount(monthItems.size())
                            .build();
                })
                .sorted(Comparator.comparing(MonthlyCostDto::getYearMonth))
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private String getVehicleLabel(VehicleDto vehicle) {
        if (vehicle.getNickname() != null && !vehicle.getNickname().isEmpty()) {
            return vehicle.getNickname();
        }
        return String.format("%d %s %s",
                vehicle.getYear() != null ? vehicle.getYear() : 0,
                vehicle.getMake() != null ? vehicle.getMake() : "",
                vehicle.getModel() != null ? vehicle.getModel() : "").trim();
    }

    private String getCategoryKey(Integer categoryId) {
        if (categoryId == null) return null;
        try {
            UpgradeCategoryDto category = upgradeCategoryService.getUpgradeCategoryById(categoryId);
            return category != null ? category.getKey() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
