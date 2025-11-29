package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.service.external.VinDecodingService;
import com.sentinovo.carbuildervin.service.external.VinDecodingService.VinDecodingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Web controller for VIN decoding - returns HTML fragments for HTMX
 */
@Controller
@RequestMapping("/vin")
@RequiredArgsConstructor
@Slf4j
public class VinWebController {

    private final VinDecodingService vinDecodingService;

    /**
     * Decode VIN and return HTML fragment with auto-populated form fields.
     * Called via HTMX from the vehicle modal VIN input field.
     */
    @PostMapping("/decode")
    public String decodeVinForForm(@RequestParam String vin, Model model) {
        log.debug("Decoding VIN for form auto-populate: {}", vin);

        // Pass through the VIN value
        model.addAttribute("vin", vin);

        // Check if VIN is too short to decode
        if (vin == null || vin.trim().length() < 17) {
            // Return fragment with empty fields - user is still typing
            model.addAttribute("vinValid", false);
            model.addAttribute("vinError", null);
            return "builds/fragments/vehicle-details-fragment";
        }

        try {
            VinDecodingResponse decoded = vinDecodingService.decodeVin(vin.trim());

            model.addAttribute("year", decoded.getYear());
            model.addAttribute("make", decoded.getMake());
            model.addAttribute("model", decoded.getModel());
            model.addAttribute("trim", decoded.getTrim());
            model.addAttribute("vinValid", true);
            model.addAttribute("vinError", null);

            log.info("VIN decoded successfully: {} -> {} {} {}",
                    vin, decoded.getYear(), decoded.getMake(), decoded.getModel());

        } catch (Exception e) {
            log.warn("VIN decode failed for {}: {}", vin, e.getMessage());

            // Return fragment with error message but keep form usable
            model.addAttribute("vinValid", false);
            model.addAttribute("vinError", "Could not decode VIN: " + e.getMessage());
        }

        return "builds/fragments/vehicle-details-fragment";
    }
}
