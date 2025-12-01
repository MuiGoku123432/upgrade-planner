package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.dto.parts.PartDto;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Web controller for part-related HTMX operations.
 * Handles endpoints called directly by HTMX templates for parts.
 */
@Controller
@RequestMapping("/parts")
@RequiredArgsConstructor
@Slf4j
public class PartsWebController {

    private final PartService partService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final VehicleService vehicleService;
    private final AuthenticationService authenticationService;

    /**
     * Update part status (HTMX)
     * Called from status dropdowns in parts-table and kanban views.
     * Returns the updated part row fragment for parts-table (outerHTML swap),
     * or empty response for kanban (swap="none").
     */
    @PatchMapping("/{partId}/status")
    public String updatePartStatus(@PathVariable UUID partId,
                                   @RequestParam String status,
                                   Model model,
                                   HttpServletResponse response) {

        log.info("Updating part {} status to {}", partId, status);

        try {
            // Verify ownership before update
            PartDto currentPart = partService.getPartById(partId);
            var build = vehicleUpgradeService.getVehicleUpgradeById(currentPart.getVehicleUpgradeId());
            String username = authenticationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"))
                    .getUsername();
            vehicleService.verifyOwnership(build.getVehicleId(), username);

            // Update the part status
            PartDto updatedPart = partService.updatePartStatusDto(partId, status);

            // Set HX-Trigger header for any listeners
            response.setHeader("HX-Trigger", "partUpdated");

            // Return the updated row fragment for parts-table view
            model.addAttribute("part", updatedPart);
            return "parts/fragments/part-row";

        } catch (Exception e) {
            log.error("Error updating part status: {}", partId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    /**
     * Delete part (HTMX)
     * Returns empty response for outerHTML swap (removes the row).
     */
    @DeleteMapping("/{partId}")
    @ResponseBody
    public void deletePart(@PathVariable UUID partId,
                          HttpServletResponse response) {
        log.info("Deleting part: {}", partId);

        try {
            // Verify ownership before delete
            PartDto currentPart = partService.getPartById(partId);
            var build = vehicleUpgradeService.getVehicleUpgradeById(currentPart.getVehicleUpgradeId());
            String username = authenticationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"))
                    .getUsername();
            vehicleService.verifyOwnership(build.getVehicleId(), username);

            partService.deletePart(partId);
            log.info("Part deleted successfully: {}", partId);

            // Set HX-Trigger header for any listeners
            response.setHeader("HX-Trigger", "partDeleted");

        } catch (Exception e) {
            log.error("Error deleting part: {}", partId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
