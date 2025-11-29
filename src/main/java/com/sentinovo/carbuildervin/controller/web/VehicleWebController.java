package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleCreateDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleDto;
import com.sentinovo.carbuildervin.dto.vehicle.VehicleUpdateDto;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Web controller for vehicle-related HTML pages using HTMX
 */
@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleWebController {

    private final VehicleService vehicleService;
    private final AuthenticationService authenticationService;

    /**
     * Display vehicles list page
     */
    @GetMapping
    public String vehiclesList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String search,
                              Model model,
                              HttpServletRequest request) {
        
        // Create pageable with default sorting
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get current user ID
        UUID currentUserId = authenticationService.getCurrentUserId();
        
        // Get vehicles page
        PageResponseDto<VehicleDto> vehiclesPage;
        if (search != null && !search.trim().isEmpty()) {
            log.debug("Searching vehicles with query: {}", search);
            vehiclesPage = vehicleService.searchUserVehiclesDto(currentUserId, search, pageable);
        } else {
            vehiclesPage = vehicleService.getUserVehiclesPaged(currentUserId, pageable);
        }
        
        model.addAttribute("vehiclesPage", vehiclesPage);
        model.addAttribute("currentSearch", search);
        model.addAttribute("pageTitle", "Vehicle Management");
        model.addAttribute("currentPath", request.getRequestURI());
        
        // If this is an HTMX request for search, return just the vehicle list fragment
        String htmxRequest = request.getHeader("HX-Request");
        if ("true".equals(htmxRequest) && search != null) {
            return "fragments/vehicle-list";
        }
        
        return "vehicles/list";
    }

    /**
     * Display vehicle detail page
     */
    @GetMapping("/{id}")
    public String vehicleDetail(@PathVariable UUID id,
                               Model model,
                               HttpServletRequest request) {
        
        VehicleDto vehicle = vehicleService.getVehicleById(id);
        
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("pageTitle", "Vehicle: " + vehicle.getYear() + " " + 
                          vehicle.getMake() + " " + vehicle.getModel());
        model.addAttribute("currentPath", request.getRequestURI());
        
        return "vehicles/detail";
    }

    /**
     * Show add vehicle modal (HTMX)
     */
    @GetMapping("/add")
    public String showAddVehicleModal(Model model) {
        model.addAttribute("vehicle", new VehicleCreateDto());
        model.addAttribute("isEdit", false);
        return "builds/modals/vehicle-modal";
    }

    /**
     * Show edit vehicle modal (HTMX)
     */
    @GetMapping("/{id}/edit")
    public String showEditVehicleModal(@PathVariable UUID id, Model model) {
        VehicleDto vehicle = vehicleService.getVehicleById(id);
        
        // Convert to update DTO
        VehicleUpdateDto updateDto = new VehicleUpdateDto();
        updateDto.setVin(vehicle.getVin());
        updateDto.setYear(vehicle.getYear());
        updateDto.setMake(vehicle.getMake());
        updateDto.setModel(vehicle.getModel());
        updateDto.setTrim(vehicle.getTrim());
        updateDto.setNotes(vehicle.getNotes());
        
        model.addAttribute("vehicle", updateDto);
        model.addAttribute("vehicleId", id);
        model.addAttribute("isEdit", true);
        return "builds/modals/vehicle-modal";
    }

    /**
     * Create new vehicle (HTMX)
     */
    @PostMapping
    public String createVehicle(@Valid @ModelAttribute VehicleCreateDto vehicleDto,
                               Model model,
                               HttpServletRequest request) {
        
        try {
            VehicleDto createdVehicle = vehicleService.createVehicle(vehicleDto);
            log.info("Vehicle created successfully: {}", createdVehicle.getId());
            
            // Add success notification
            model.addAttribute("notification", "Vehicle added successfully!");
            model.addAttribute("notificationType", "success");
            
            // Return updated vehicle list
            return "redirect:/vehicles";
            
        } catch (Exception e) {
            log.error("Error creating vehicle", e);
            
            // Return modal with error
            model.addAttribute("vehicle", vehicleDto);
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Failed to create vehicle: " + e.getMessage());
            return "builds/modals/vehicle-modal";
        }
    }

    /**
     * Update vehicle (HTMX)
     */
    @PutMapping("/{id}")
    public String updateVehicle(@PathVariable UUID id,
                               @Valid @ModelAttribute VehicleUpdateDto vehicleDto,
                               Model model) {
        
        try {
            VehicleDto updatedVehicle = vehicleService.updateVehicle(id, vehicleDto);
            log.info("Vehicle updated successfully: {}", updatedVehicle.getId());
            
            // Add success notification
            model.addAttribute("notification", "Vehicle updated successfully!");
            model.addAttribute("notificationType", "success");
            
            // Return to vehicle detail
            return "redirect:/vehicles/" + id;
            
        } catch (Exception e) {
            log.error("Error updating vehicle with ID: {}", id, e);
            
            // Return modal with error
            model.addAttribute("vehicle", vehicleDto);
            model.addAttribute("vehicleId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update vehicle: " + e.getMessage());
            return "builds/modals/vehicle-modal";
        }
    }

    /**
     * Delete vehicle (HTMX) - returns empty response for smooth UI removal
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleService.archiveVehicleDto(id);
        log.info("Vehicle archived successfully: {}", id);
        // Empty response = HTMX removes element via hx-swap="outerHTML"
    }

    /**
     * Get vehicle card fragment for HTMX updates
     */
    @GetMapping("/{id}/card")
    public String vehicleCard(@PathVariable UUID id, Model model) {
        VehicleDto vehicle = vehicleService.getVehicleById(id);
        model.addAttribute("vehicle", vehicle);
        return "fragments/vehicle-card";
    }
}