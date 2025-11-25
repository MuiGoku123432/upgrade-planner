package com.sentinovo.carbuildervin.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for serving HTML pages with Thymeleaf templates
 */
@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Car Builder VIN System");
        model.addAttribute("systemStatus", "ONLINE");
        model.addAttribute("currentPath", request.getRequestURI());
        return "index";
    }

    @GetMapping("/demo")
    public String demo(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Jarvis System Interface - Demo");
        model.addAttribute("operatorName", "T. Stark");
        model.addAttribute("systemStatus", "OPERATIONAL");
        model.addAttribute("currentPath", request.getRequestURI());
        return "demo";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Vehicle Dashboard");
        model.addAttribute("vehicleCount", 3);
        model.addAttribute("activeBuilds", 5);
        model.addAttribute("currentPath", request.getRequestURI());
        return "dashboard";
    }
}