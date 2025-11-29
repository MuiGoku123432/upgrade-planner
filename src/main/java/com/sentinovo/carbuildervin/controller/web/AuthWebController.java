package com.sentinovo.carbuildervin.controller.web;

import com.sentinovo.carbuildervin.dto.auth.RegisterRequestDto;
import com.sentinovo.carbuildervin.dto.auth.UserDto;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Simple HTMX authentication controller
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthWebController {

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        
        return "login";
    }

    /**
     * Show signup page
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "signup";
    }

    /**
     * Handle login form submission
     */
    @PostMapping("/login")
    public String handleLogin(@RequestParam String usernameOrEmail,
                             @RequestParam String password,
                             @RequestParam(required = false) String rememberMe,
                             Model model,
                             HttpServletRequest request) {
        
        try {
            // Authenticate user
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(usernameOrEmail, password);
            
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Save to session
            request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext()
            );
            
            log.info("User authenticated successfully: {}", usernameOrEmail);
            
            // Redirect to builds page
            return "redirect:/builds";
            
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", usernameOrEmail, e);
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    /**
     * Handle signup form submission
     */
    @PostMapping("/signup")
    public String handleSignup(@Valid @ModelAttribute RegisterRequestDto registerRequest,
                              BindingResult bindingResult,
                              Model model,
                              HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            log.debug("Signup validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("error", "Please correct the errors below");
            model.addAttribute("registerRequest", registerRequest);
            return "signup";
        }
        
        try {
            UserDto user = authenticationService.registerUser(registerRequest);
            log.info("User registered successfully: {}", user.getUsername());
            
            // Auto-login after registration
            try {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(), 
                        registerRequest.getPassword()
                    );
                
                Authentication authentication = authenticationManager.authenticate(authToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                    SecurityContextHolder.getContext()
                );
                
                log.info("User auto-logged in after registration: {}", user.getUsername());
                return "redirect:/builds";
                
            } catch (Exception authException) {
                log.warn("Auto-login failed, redirecting to login", authException);
                model.addAttribute("message", "Registration successful! Please login.");
                return "login";
            }
            
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    /**
     * Handle logout
     */
    @PostMapping("/logout")
    public String handleLogout(HttpServletRequest request, HttpServletResponse response) {
        // Clear security context
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        
        // Check if this is an HTMX request (for logout from authenticated pages)
        if (isHtmxRequest(request)) {
            response.setHeader("HX-Redirect", "/login?logout=true");
            return null; // Empty response, let HTMX handle redirect
        }
        
        return "redirect:/login?logout=true";
    }


    // Helper methods
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}