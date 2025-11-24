package com.sentinovo.carbuildervin.services.parts;

import com.sentinovo.carbuildervin.entities.parts.PartCategory;
import com.sentinovo.carbuildervin.exception.DuplicateResourceException;
import com.sentinovo.carbuildervin.exception.InvalidStateException;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.repository.parts.PartCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PartCategoryService {

    private final PartCategoryRepository partCategoryRepository;

    @Transactional(readOnly = true)
    public PartCategory findByCode(String code) {
        return partCategoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("PartCategory", code));
    }

    @Transactional(readOnly = true)
    public Optional<PartCategory> findByCodeOptional(String code) {
        return partCategoryRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<PartCategory> findAllCategories() {
        return partCategoryRepository.findAllOrderBySortOrderAndLabel();
    }

    @Transactional(readOnly = true)
    public List<PartCategory> searchCategories(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllCategories();
        }
        return partCategoryRepository.searchByCodeLabelOrDescription(searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public long countPartsInCategory(String categoryCode) {
        return partCategoryRepository.countPartsByCategoryCode(categoryCode);
    }

    @Transactional(readOnly = true)
    public long countSubPartsInCategory(String categoryCode) {
        return partCategoryRepository.countSubPartsByCategoryCode(categoryCode);
    }

    @Transactional(readOnly = true)
    public long countTotalItemsInCategory(String categoryCode) {
        return countPartsInCategory(categoryCode) + countSubPartsInCategory(categoryCode);
    }

    public PartCategory createCategory(String code, String label, String description, Integer sortOrder) {
        log.info("Creating new part category with code: {}", code);
        
        validateCategoryCreation(code);
        
        if (sortOrder == null) {
            Integer maxSortOrder = partCategoryRepository.findMaxSortOrder();
            sortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;
        }
        
        PartCategory category = PartCategory.builder()
                .code(code.toUpperCase())
                .label(label)
                .description(description)
                .sortOrder(sortOrder)
                .build();

        PartCategory savedCategory = partCategoryRepository.save(category);
        log.info("Successfully created part category with code: {}", savedCategory.getCode());
        return savedCategory;
    }

    public PartCategory updateCategory(String code, String label, String description, Integer sortOrder) {
        log.info("Updating part category with code: {}", code);
        
        PartCategory category = findByCode(code);
        
        if (label != null) category.setLabel(label);
        if (description != null) category.setDescription(description);
        if (sortOrder != null) category.setSortOrder(sortOrder);

        PartCategory savedCategory = partCategoryRepository.save(category);
        log.info("Successfully updated part category with code: {}", savedCategory.getCode());
        return savedCategory;
    }

    public PartCategory updateCategorySortOrder(String code, Integer sortOrder) {
        log.info("Updating sort order for part category with code: {} to {}", code, sortOrder);
        
        PartCategory category = findByCode(code);
        category.setSortOrder(sortOrder);
        
        PartCategory savedCategory = partCategoryRepository.save(category);
        log.info("Successfully updated sort order for part category with code: {}", savedCategory.getCode());
        return savedCategory;
    }

    public void deleteCategory(String code) {
        log.info("Deleting part category with code: {}", code);
        
        PartCategory category = findByCode(code);
        
        long totalItems = countTotalItemsInCategory(code);
        if (totalItems > 0) {
            throw new InvalidStateException(
                String.format("Cannot delete category '%s' - it is used by %d part(s)", 
                    category.getLabel(), totalItems)
            );
        }
        
        partCategoryRepository.delete(category);
        log.info("Successfully deleted part category with code: {}", code);
    }

    @Transactional(readOnly = true)
    public boolean isCategoryCodeAvailable(String code) {
        return !partCategoryRepository.existsByCode(code.toUpperCase());
    }

    public void ensureDefaultCategories() {
        log.info("Ensuring default part categories exist");
        
        String[][] defaultCategories = {
            {"ENGINE", "Engine", "Engine components and modifications"},
            {"SUSPENSION", "Suspension", "Suspension components and upgrades"},
            {"BRAKES", "Brakes", "Brake system components"},
            {"WHEELS", "Wheels & Tires", "Wheels, tires, and related components"},
            {"EXHAUST", "Exhaust", "Exhaust system components"},
            {"INTAKE", "Intake", "Air intake system components"},
            {"TURBO", "Turbocharger", "Turbocharger and related components"},
            {"COOLING", "Cooling", "Cooling system components"},
            {"FUEL", "Fuel System", "Fuel system components"},
            {"IGNITION", "Ignition", "Ignition system components"},
            {"TRANSMISSION", "Transmission", "Transmission and drivetrain components"},
            {"INTERIOR", "Interior", "Interior components and accessories"},
            {"EXTERIOR", "Exterior", "Exterior components and accessories"},
            {"ELECTRONICS", "Electronics", "Electronic components and accessories"},
            {"TOOLS", "Tools", "Tools and equipment"},
            {"MAINTENANCE", "Maintenance", "Maintenance items and fluids"},
            {"SAFETY", "Safety", "Safety equipment and components"},
            {"OTHER", "Other", "Other miscellaneous parts"}
        };
        
        for (String[] categoryData : defaultCategories) {
            if (findByCodeOptional(categoryData[0]).isEmpty()) {
                createCategory(categoryData[0], categoryData[1], categoryData[2], null);
            }
        }
        
        log.info("Default part categories ensured");
    }

    private void validateCategoryCreation(String code) {
        if (partCategoryRepository.existsByCode(code.toUpperCase())) {
            throw new DuplicateResourceException("PartCategory", "code", code);
        }
    }
}