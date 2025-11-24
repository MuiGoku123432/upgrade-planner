package com.sentinovo.carbuildervin.config;

import com.sentinovo.carbuildervin.services.parts.PartCategoryService;
import com.sentinovo.carbuildervin.services.parts.PartTierService;
import com.sentinovo.carbuildervin.services.user.RoleService;
import com.sentinovo.carbuildervin.services.vehicle.UpgradeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializationConfig {

    private final RoleService roleService;
    private final UpgradeCategoryService upgradeCategoryService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;

    @Bean
    @Order(1)
    public CommandLineRunner initializeDefaultData() {
        return args -> {
            log.info("Initializing default application data...");
            
            try {
                // Initialize default roles
                roleService.ensureDefaultRoles();
                log.info("✓ Default roles initialized");
                
                // Initialize default upgrade categories
                upgradeCategoryService.ensureDefaultCategories();
                log.info("✓ Default upgrade categories initialized");
                
                // Initialize default part categories
                partCategoryService.ensureDefaultCategories();
                log.info("✓ Default part categories initialized");
                
                // Initialize default part tiers
                partTierService.ensureDefaultTiers();
                log.info("✓ Default part tiers initialized");
                
                log.info("Default application data initialization completed successfully");
                
            } catch (Exception e) {
                log.error("Error during default data initialization: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize default application data", e);
            }
        };
    }
}