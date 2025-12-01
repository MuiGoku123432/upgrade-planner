package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.status.BuildStatusDetailDto;
import com.sentinovo.carbuildervin.dto.status.BuildStatusOverviewDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.vehicle.UpgradeCategoryService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web controller for status/progress page - dashboard view of build progress
 */
@Controller
@RequestMapping("/status")
@RequiredArgsConstructor
@Slf4j
public class StatusWebController {

    private final VehicleService vehicleService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final PartService partService;
    private final UpgradeCategoryService upgradeCategoryService;
    private final AuthenticationService authenticationService;

    // Part status order for Kanban display
    private static final List<String> STATUS_ORDER = Arrays.asList(
            "PLANNED", "RESEARCHING", "ORDERED", "DELIVERED", "INSTALLED", "CANCELLED"
    );

    /**
     * Main status overview page
     */
    @GetMapping
    public String statusPage(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading status page for user: {}", currentUserId);

        // Get filter dropdown data
        List<VehicleDto> userVehicles = vehicleService.getUserVehicles(currentUserId);
        List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();

        model.addAttribute("vehicles", userVehicles);
        model.addAttribute("categories", categories);
        model.addAttribute("buildStatuses", Arrays.asList("PLANNED", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"));

        return "status/index";
    }

    /**
     * HTMX fragment - filtered build status overview cards
     */
    @GetMapping("/fragment/overview")
    public String getOverviewFragment(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) String categoryKey,
            @RequestParam(required = false) String buildStatus,
            Model model) {

        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading status overview fragment for user: {}, vehicleId: {}, categoryKey: {}, buildStatus: {}",
                currentUserId, vehicleId, categoryKey, buildStatus);

        // Get all user's vehicles
        List<VehicleDto> userVehicles = vehicleService.getUserVehicles(currentUserId);

        // Filter by vehicle if specified
        if (vehicleId != null) {
            userVehicles = userVehicles.stream()
                    .filter(v -> v.getId().equals(vehicleId))
                    .collect(Collectors.toList());
        }

        // Collect all builds with status calculations
        List<BuildStatusOverviewDto> buildOverviews = new ArrayList<>();

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
                BuildStatusOverviewDto overview = calculateBuildOverview(build, vehicle);
                buildOverviews.add(overview);
            }
        }

        // Sort by priority level, then by name
        buildOverviews.sort(Comparator
                .comparing(BuildStatusOverviewDto::getPriorityLevel, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(BuildStatusOverviewDto::getBuildName));

        model.addAttribute("buildOverviews", buildOverviews);

        return "status/fragments/overview :: overview";
    }

    /**
     * Build detail status page
     */
    @GetMapping("/build/{buildId}")
    public String buildStatusPage(@PathVariable UUID buildId, Model model) {
        log.debug("Loading build status page for build: {}", buildId);

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        VehicleDto vehicle = vehicleService.getVehicleById(build.getVehicleId());

        model.addAttribute("build", build);
        model.addAttribute("vehicleLabel", getVehicleLabel(vehicle));

        return "status/build";
    }

    /**
     * HTMX fragment - build summary chips
     */
    @GetMapping("/fragment/build-summary/{buildId}")
    public String getBuildSummaryFragment(@PathVariable UUID buildId, Model model) {
        log.debug("Loading build summary fragment for build: {}", buildId);

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        VehicleDto vehicle = vehicleService.getVehicleById(build.getVehicleId());
        List<PartDto> parts = partService.getPartsByUpgradeId(buildId);

        BuildStatusDetailDto detail = calculateBuildDetail(build, vehicle, parts);
        model.addAttribute("detail", detail);

        return "status/fragments/build-summary :: build-summary";
    }

    /**
     * HTMX fragment - build Kanban columns
     */
    @GetMapping("/fragment/build-kanban/{buildId}")
    public String getBuildKanbanFragment(@PathVariable UUID buildId, Model model) {
        log.debug("Loading build Kanban fragment for build: {}", buildId);

        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        VehicleDto vehicle = vehicleService.getVehicleById(build.getVehicleId());
        List<PartDto> parts = partService.getPartsByUpgradeId(buildId);

        BuildStatusDetailDto detail = calculateBuildDetail(build, vehicle, parts);

        model.addAttribute("detail", detail);
        model.addAttribute("buildId", buildId);
        model.addAttribute("statusOrder", STATUS_ORDER);

        return "status/fragments/build-kanban :: build-kanban";
    }

    // ==================== Helper Methods ====================

    private BuildStatusOverviewDto calculateBuildOverview(VehicleUpgradeDto build, VehicleDto vehicle) {
        List<PartDto> parts = partService.getPartsByUpgradeId(build.getId());
        LocalDate today = LocalDate.now();

        long requiredTotal = parts.stream().filter(p -> Boolean.TRUE.equals(p.getIsRequired())).count();
        long requiredInstalled = parts.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsRequired()) && "INSTALLED".equals(p.getStatus()))
                .count();

        long optionalTotal = parts.stream().filter(p -> !Boolean.TRUE.equals(p.getIsRequired())).count();
        long optionalInstalled = parts.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsRequired()) && "INSTALLED".equals(p.getStatus()))
                .count();

        double percentRequired = requiredTotal == 0 ? 0 : (requiredInstalled * 100.0 / requiredTotal);
        double percentOptional = optionalTotal == 0 ? 0 : (optionalInstalled * 100.0 / optionalTotal);

        long overdueCount = parts.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsRequired())
                        && !"INSTALLED".equals(p.getStatus())
                        && p.getTargetPurchaseDate() != null
                        && p.getTargetPurchaseDate().isBefore(today))
                .count();

        UpgradeCategoryDto category = null;
        if (build.getUpgradeCategoryId() != null) {
            try {
                category = upgradeCategoryService.getUpgradeCategoryById(build.getUpgradeCategoryId());
            } catch (Exception e) {
                log.warn("Could not fetch category for build {}: {}", build.getId(), e.getMessage());
            }
        }

        return BuildStatusOverviewDto.builder()
                .buildId(build.getId())
                .vehicleId(vehicle.getId())
                .vehicleLabel(getVehicleLabel(vehicle))
                .upgradeCategoryId(build.getUpgradeCategoryId())
                .upgradeCategoryKey(category != null ? category.getKey() : null)
                .upgradeCategoryName(build.getUpgradeCategoryName())
                .buildName(build.getName())
                .status(build.getStatus())
                .priorityLevel(build.getPriorityLevel())
                .targetCompletionDate(build.getTargetCompletionDate())
                .requiredPartsTotal(requiredTotal)
                .requiredPartsInstalled(requiredInstalled)
                .optionalPartsTotal(optionalTotal)
                .optionalPartsInstalled(optionalInstalled)
                .percentRequiredInstalled(Math.round(percentRequired * 10.0) / 10.0)
                .percentOptionalInstalled(Math.round(percentOptional * 10.0) / 10.0)
                .overdueRequiredCount(overdueCount)
                .createdAt(build.getCreatedAt())
                .updatedAt(build.getUpdatedAt())
                .build();
    }

    private BuildStatusDetailDto calculateBuildDetail(VehicleUpgradeDto build, VehicleDto vehicle, List<PartDto> parts) {
        LocalDate today = LocalDate.now();

        // Group parts by status
        Map<String, List<PartDto>> partsByStatus = parts.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus() != null ? p.getStatus() : "PLANNED",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Ensure all statuses have an entry (even if empty)
        for (String status : STATUS_ORDER) {
            partsByStatus.putIfAbsent(status, new ArrayList<>());
        }

        long requiredTotal = parts.stream().filter(p -> Boolean.TRUE.equals(p.getIsRequired())).count();
        long requiredInstalled = parts.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsRequired()) && "INSTALLED".equals(p.getStatus()))
                .count();

        long optionalTotal = parts.stream().filter(p -> !Boolean.TRUE.equals(p.getIsRequired())).count();
        long optionalInstalled = parts.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsRequired()) && "INSTALLED".equals(p.getStatus()))
                .count();

        double percentRequired = requiredTotal == 0 ? 0 : (requiredInstalled * 100.0 / requiredTotal);
        double percentOptional = optionalTotal == 0 ? 0 : (optionalInstalled * 100.0 / optionalTotal);

        long overdueCount = parts.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsRequired())
                        && !"INSTALLED".equals(p.getStatus())
                        && p.getTargetPurchaseDate() != null
                        && p.getTargetPurchaseDate().isBefore(today))
                .count();

        return BuildStatusDetailDto.builder()
                .build(build)
                .vehicleLabel(getVehicleLabel(vehicle))
                .partsByStatus(partsByStatus)
                .totalPartsCount(parts.size())
                .requiredPartsTotal(requiredTotal)
                .requiredPartsInstalled(requiredInstalled)
                .optionalPartsTotal(optionalTotal)
                .optionalPartsInstalled(optionalInstalled)
                .percentRequiredInstalled(Math.round(percentRequired * 10.0) / 10.0)
                .percentOptionalInstalled(Math.round(percentOptional * 10.0) / 10.0)
                .overdueRequiredCount(overdueCount)
                .build();
    }

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
