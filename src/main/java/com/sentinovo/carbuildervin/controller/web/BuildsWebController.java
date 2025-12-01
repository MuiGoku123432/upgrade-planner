package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeCreateDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeDto;
import com.sentinovo.carbuildervin.dto.build.VehicleUpgradeUpdateDto;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.parts.PartCreateDto;
import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.dto.parts.PartUpdateDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartCategoryDto;
import com.sentinovo.carbuildervin.dto.parts.lookup.PartTierDto;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleCreateDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleUpdateDto;
import com.sentinovo.carbuildervin.service.parts.PartCategoryService;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.parts.PartTierService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.vehicle.UpgradeCategoryService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Web controller for builds page - main workspace for vehicle build management
 */
@Controller
@RequestMapping("/builds")
@RequiredArgsConstructor
@Slf4j
public class BuildsWebController {

    private final VehicleService vehicleService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final PartService partService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;
    private final UpgradeCategoryService upgradeCategoryService;
    private final AuthenticationService authenticationService;

    /**
     * Main builds page - accordion-based vehicle build management
     */
    @GetMapping
    public String buildsPage(Model model) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.debug("Loading builds page for user: {}", currentUserId);

        // Get user's vehicles for the accordion list
        List<VehicleDto> userVehicles = vehicleService.getUserVehicles(currentUserId);
        model.addAttribute("userVehicles", userVehicles);
        model.addAttribute("vehicleCount", userVehicles.size());

        return "builds/index";
    }

    /**
     * Get builds for a vehicle (HTMX fragment for accordion)
     */
    @GetMapping("/vehicle/{vehicleId}/builds")
    public String getVehicleBuildsFragment(@PathVariable UUID vehicleId, Model model) {
        log.debug("Loading builds fragment for vehicle: {}", vehicleId);

        // Get builds for this vehicle
        List<VehicleUpgradeDto> builds = vehicleUpgradeService.getVehicleUpgradesByVehicleId(vehicleId);
        model.addAttribute("builds", builds);
        model.addAttribute("vehicleId", vehicleId);

        return "builds/fragments/vehicle-builds :: vehicle-builds";
    }

    /**
     * Get parts table for a build (HTMX fragment for accordion)
     */
    @GetMapping("/{buildId}/parts-table")
    public String getPartsTableFragment(@PathVariable UUID buildId, Model model) {
        log.debug("Loading parts table for build: {}", buildId);

        // Get parts for this build
        Pageable pageable = PageRequest.of(0, 100, Sort.by("priorityValue", "createdAt"));
        PageResponseDto<PartDto> partsPage = partService.getPartsByUpgradeIdPaged(buildId, pageable);
        model.addAttribute("parts", partsPage.getItems());
        model.addAttribute("buildId", buildId);

        return "builds/fragments/parts-table :: parts-table";
    }

    /**
     * Show add vehicle modal (HTMX)
     */
    @GetMapping("/modals/add-vehicle")
    public String showAddVehicleModal(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("vehicle", new VehicleCreateDto());
        model.addAttribute("isEdit", false);
        return "builds/modals/vehicle-modal";
    }

    /**
     * Show add build modal (HTMX)
     */
    @GetMapping("/modals/add-build")
    public String showAddBuildModal(@RequestParam UUID vehicleId, Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("isEdit", false);

        // Get vehicle details
        VehicleDto vehicle = vehicleService.getVehicleById(vehicleId);
        model.addAttribute("selectedVehicle", vehicle);

        // Get available upgrade categories
        List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();
        model.addAttribute("upgradeCategories", categories);

        // Create new build DTO
        VehicleUpgradeCreateDto newBuild = new VehicleUpgradeCreateDto();
        model.addAttribute("build", newBuild);

        return "builds/modals/build-modal";
    }

    /**
     * Show add part modal (HTMX)
     */
    @GetMapping("/modals/add-part")
    public String showAddPartModal(@RequestParam UUID buildId, Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("buildId", buildId);

        // Get lookup data for dropdowns
        List<PartCategoryDto> partCategories = partCategoryService.getAllPartCategories();
        List<PartTierDto> partTiers = partTierService.getAllPartTiers();

        model.addAttribute("partCategories", partCategories);
        model.addAttribute("partTiers", partTiers);
        model.addAttribute("part", new PartCreateDto());

        return "builds/modals/part-modal";
    }

    /**
     * Create new build (HTMX)
     */
    @PostMapping("/build")
    public String createBuild(@RequestParam UUID vehicleId,
                             @Valid @ModelAttribute VehicleUpgradeCreateDto buildDto,
                             BindingResult bindingResult,
                             Model model,
                             CsrfToken csrfToken,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.debug("Build creation validation errors: {}", bindingResult.getAllErrors());

            // Get vehicle and categories for redisplay
            VehicleDto vehicle = vehicleService.getVehicleById(vehicleId);
            List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("selectedVehicle", vehicle);
            model.addAttribute("upgradeCategories", categories);
            model.addAttribute("build", buildDto);
            model.addAttribute("error", "Please correct the errors below");

            return "builds/modals/build-modal";
        }

        try {
            VehicleUpgradeDto createdBuild = vehicleUpgradeService.createVehicleUpgrade(vehicleId, buildDto);
            log.info("Build created successfully: {}", createdBuild.getId());

            // If HTMX request, trigger refresh of build section
            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "buildCreated");
                // Return null to signal response is handled - closes modal via HTMX event
                return null;
            }

            return "redirect:/builds?vehicleId=" + vehicleId + "&buildId=" + createdBuild.getId();

        } catch (Exception e) {
            log.error("Error creating build", e);

            // Get vehicle and categories for redisplay
            VehicleDto vehicle = vehicleService.getVehicleById(vehicleId);
            List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("selectedVehicle", vehicle);
            model.addAttribute("upgradeCategories", categories);
            model.addAttribute("build", buildDto);
            model.addAttribute("error", "Failed to create build: " + e.getMessage());

            return "builds/modals/build-modal";
        }
    }

    /**
     * Create new part (HTMX)
     */
    @PostMapping("/part")
    public String createPart(@RequestParam UUID buildId,
                            @Valid @ModelAttribute PartCreateDto partDto,
                            BindingResult bindingResult,
                            Model model,
                            CsrfToken csrfToken,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.debug("Part creation validation errors: {}", bindingResult.getAllErrors());

            // Get lookup data for redisplay
            List<PartCategoryDto> partCategories = partCategoryService.getAllPartCategories();
            List<PartTierDto> partTiers = partTierService.getAllPartTiers();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("buildId", buildId);
            model.addAttribute("partCategories", partCategories);
            model.addAttribute("partTiers", partTiers);
            model.addAttribute("part", partDto);
            model.addAttribute("error", "Please correct the errors below");

            return "builds/modals/part-modal";
        }

        try {
            PartDto createdPart = partService.createPart(buildId, partDto);
            log.info("Part created successfully: {}", createdPart.getId());

            // If HTMX request, trigger refresh of parts table
            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "partCreated");
                // Return null to signal response is handled - closes modal via HTMX event
                return null;
            }

            return "redirect:/builds";

        } catch (Exception e) {
            log.error("Error creating part", e);

            // Get lookup data for redisplay
            List<PartCategoryDto> partCategories = partCategoryService.getAllPartCategories();
            List<PartTierDto> partTiers = partTierService.getAllPartTiers();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("buildId", buildId);
            model.addAttribute("partCategories", partCategories);
            model.addAttribute("partTiers", partTiers);
            model.addAttribute("part", partDto);
            model.addAttribute("error", "Failed to create part: " + e.getMessage());

            return "builds/modals/part-modal";
        }
    }

    /**
     * Create new vehicle from builds page (HTMX)
     */
    @PostMapping("/vehicle")
    public String createVehicle(@Valid @ModelAttribute VehicleCreateDto vehicleDto,
                               BindingResult bindingResult,
                               Model model,
                               CsrfToken csrfToken,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.debug("Vehicle creation validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("vehicle", vehicleDto);
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Please correct the errors below");
            return "builds/modals/vehicle-modal";
        }

        try {
            VehicleDto createdVehicle = vehicleService.createVehicle(vehicleDto);
            log.info("Vehicle created successfully: {}", createdVehicle.getId());

            // If HTMX request, trigger vehicleCreated event
            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "vehicleCreated");
                // Return null to signal response is handled - closes modal via HTMX event
                return null;
            }

            return "redirect:/builds?vehicleId=" + createdVehicle.getId();

        } catch (Exception e) {
            log.error("Error creating vehicle", e);
            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("vehicle", vehicleDto);
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Failed to create vehicle: " + e.getMessage());
            return "builds/modals/vehicle-modal";
        }
    }

    /**
     * Update vehicle (HTMX)
     */
    @PutMapping("/vehicle/{vehicleId}")
    public String updateVehicle(@PathVariable UUID vehicleId,
                               @Valid @ModelAttribute VehicleUpdateDto vehicleDto,
                               BindingResult bindingResult,
                               Model model,
                               CsrfToken csrfToken,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.debug("Vehicle update validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("vehicle", vehicleDto);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Please correct the errors below");
            return "builds/modals/vehicle-modal";
        }

        try {
            VehicleDto updatedVehicle = vehicleService.updateVehicle(vehicleId, vehicleDto);
            log.info("Vehicle updated successfully: {}", updatedVehicle.getId());

            // If HTMX request, trigger vehicleUpdated event
            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "vehicleUpdated");
                // Return null to signal response is handled - closes modal via HTMX event
                return null;
            }

            return "redirect:/builds";

        } catch (Exception e) {
            log.error("Error updating vehicle", e);
            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("vehicle", vehicleDto);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update vehicle: " + e.getMessage());
            return "builds/modals/vehicle-modal";
        }
    }

    // ==================== BUILD EDIT/DELETE ====================

    /**
     * Show edit build modal (HTMX)
     */
    @GetMapping("/modals/edit-build")
    public String showEditBuildModal(@RequestParam UUID buildId, Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);

        // Get existing build
        VehicleUpgradeDto build = vehicleUpgradeService.getVehicleUpgradeById(buildId);
        model.addAttribute("build", build);
        model.addAttribute("buildId", buildId);
        model.addAttribute("isEdit", true);

        // Get vehicle details
        VehicleDto vehicle = vehicleService.getVehicleById(build.getVehicleId());
        model.addAttribute("selectedVehicle", vehicle);

        // Get available upgrade categories
        List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();
        model.addAttribute("upgradeCategories", categories);

        return "builds/modals/build-modal";
    }

    /**
     * Update build (HTMX)
     */
    @PutMapping("/build/{buildId}")
    public String updateBuild(@PathVariable UUID buildId,
                             @Valid @ModelAttribute VehicleUpgradeUpdateDto buildDto,
                             BindingResult bindingResult,
                             Model model,
                             CsrfToken csrfToken,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.debug("Build update validation errors: {}", bindingResult.getAllErrors());

            VehicleUpgradeDto existingBuild = vehicleUpgradeService.getVehicleUpgradeById(buildId);
            VehicleDto vehicle = vehicleService.getVehicleById(existingBuild.getVehicleId());
            List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("selectedVehicle", vehicle);
            model.addAttribute("upgradeCategories", categories);
            model.addAttribute("build", buildDto);
            model.addAttribute("buildId", buildId);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Please correct the errors below");

            return "builds/modals/build-modal";
        }

        try {
            VehicleUpgradeDto updatedBuild = vehicleUpgradeService.updateVehicleUpgrade(buildId, buildDto);
            log.info("Build updated successfully: {}", updatedBuild.getId());

            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "buildUpdated");
                // Return null to signal response is handled - closes modal via HTMX event
                return null;
            }

            return "redirect:/builds";

        } catch (Exception e) {
            log.error("Error updating build", e);

            VehicleUpgradeDto existingBuild = vehicleUpgradeService.getVehicleUpgradeById(buildId);
            VehicleDto vehicle = vehicleService.getVehicleById(existingBuild.getVehicleId());
            List<UpgradeCategoryDto> categories = upgradeCategoryService.getActiveUpgradeCategories();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("selectedVehicle", vehicle);
            model.addAttribute("upgradeCategories", categories);
            model.addAttribute("build", buildDto);
            model.addAttribute("buildId", buildId);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update build: " + e.getMessage());

            return "builds/modals/build-modal";
        }
    }

    /**
     * Delete build (HTMX) - returns empty response for smooth UI removal
     */
    @DeleteMapping("/build/{buildId}")
    @ResponseBody
    public void deleteBuild(@PathVariable UUID buildId) {
        vehicleUpgradeService.deleteUpgrade(buildId);
        log.info("Build deleted successfully: {}", buildId);
        // Empty response = HTMX removes element via hx-swap="outerHTML"
    }

    // ==================== PART EDIT ====================

    /**
     * Show edit part modal (HTMX)
     */
    @GetMapping("/modals/edit-part")
    public String showEditPartModal(@RequestParam UUID partId, Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);

        // Get existing part
        PartDto part = partService.getPartById(partId);
        model.addAttribute("part", part);
        model.addAttribute("partId", partId);
        model.addAttribute("buildId", part.getVehicleUpgradeId());
        model.addAttribute("isEdit", true);

        // Get lookup data for dropdowns
        List<PartCategoryDto> partCategories = partCategoryService.getAllPartCategories();
        List<PartTierDto> partTiers = partTierService.getAllPartTiers();

        model.addAttribute("partCategories", partCategories);
        model.addAttribute("partTiers", partTiers);

        return "builds/modals/part-modal";
    }

    /**
     * Update part (HTMX)
     */
    @PutMapping("/part/{partId}")
    public String updatePart(@PathVariable UUID partId,
                            @Valid @ModelAttribute PartUpdateDto partDto,
                            BindingResult bindingResult,
                            Model model,
                            CsrfToken csrfToken,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.debug("Part update validation errors: {}", bindingResult.getAllErrors());

            PartDto existingPart = partService.getPartById(partId);
            List<PartCategoryDto> partCategories = partCategoryService.getAllPartCategories();
            List<PartTierDto> partTiers = partTierService.getAllPartTiers();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("partId", partId);
            model.addAttribute("buildId", existingPart.getVehicleUpgradeId());
            model.addAttribute("partCategories", partCategories);
            model.addAttribute("partTiers", partTiers);
            model.addAttribute("part", partDto);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Please correct the errors below");

            return "builds/modals/part-modal";
        }

        try {
            PartDto updatedPart = partService.updatePart(partId, partDto);
            log.info("Part updated successfully: {}", updatedPart.getId());

            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "partUpdated");
                // Return null to signal response is handled - closes modal via HTMX event
                return null;
            }

            return "redirect:/builds";

        } catch (Exception e) {
            log.error("Error updating part", e);

            PartDto existingPart = partService.getPartById(partId);
            List<PartCategoryDto> partCategories = partCategoryService.getAllPartCategories();
            List<PartTierDto> partTiers = partTierService.getAllPartTiers();

            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("partId", partId);
            model.addAttribute("buildId", existingPart.getVehicleUpgradeId());
            model.addAttribute("partCategories", partCategories);
            model.addAttribute("partTiers", partTiers);
            model.addAttribute("part", partDto);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update part: " + e.getMessage());

            return "builds/modals/part-modal";
        }
    }

    /**
     * Delete part (HTMX)
     */
    @DeleteMapping("/part/{partId}")
    @ResponseBody
    public void deletePart(@PathVariable UUID partId,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        try {
            partService.deletePart(partId);
            log.info("Part deleted successfully: {}", partId);

            if (isHtmxRequest(request)) {
                response.setHeader("HX-Trigger", "partDeleted");
            }
        } catch (Exception e) {
            log.error("Error deleting part: {}", partId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}