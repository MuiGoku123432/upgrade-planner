package com.sentinovo.carbuildervin.service.vehicle;

import com.sentinovo.carbuildervin.dto.upgrade.*;
import com.sentinovo.carbuildervin.entities.vehicle.UpgradeCategory;
import com.sentinovo.carbuildervin.exception.DuplicateResourceException;
import com.sentinovo.carbuildervin.exception.InvalidStateException;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.mapper.vehicle.UpgradeCategoryMapper;
import com.sentinovo.carbuildervin.repository.vehicle.UpgradeCategoryRepository;
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
public class UpgradeCategoryService {

    private final UpgradeCategoryRepository upgradeCategoryRepository;
    private final UpgradeCategoryMapper upgradeCategoryMapper;

    @Transactional(readOnly = true)
    public UpgradeCategory findById(Integer id) {
        return upgradeCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UpgradeCategory", id.toString()));
    }

    @Transactional(readOnly = true)
    public Optional<UpgradeCategory> findByName(String name) {
        return upgradeCategoryRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public UpgradeCategory getByName(String name) {
        return findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("UpgradeCategory", name));
    }

    @Transactional(readOnly = true)
    public List<UpgradeCategory> findAllCategories() {
        return upgradeCategoryRepository.findAllOrderBySortOrderAndName();
    }

    @Transactional(readOnly = true)
    public List<UpgradeCategory> findActiveCategories() {
        return upgradeCategoryRepository.findActiveOrderBySortOrderAndName();
    }

    @Transactional(readOnly = true)
    public List<UpgradeCategory> searchCategories(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllCategories();
        }
        return upgradeCategoryRepository.searchByNameOrDescription(searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public long countUpgradesByCategory(Integer categoryId) {
        return upgradeCategoryRepository.countVehicleUpgradesByCategoryId(categoryId);
    }

    // ===== DTO-Based Methods =====

    @Transactional(readOnly = true)
    public UpgradeCategoryDto getUpgradeCategoryById(Integer id) {
        UpgradeCategory category = findById(id);
        return upgradeCategoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    public UpgradeCategoryDto getUpgradeCategoryByName(String name) {
        UpgradeCategory category = getByName(name);
        return upgradeCategoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    public List<UpgradeCategoryDto> getAllUpgradeCategories() {
        List<UpgradeCategory> categories = findAllCategories();
        return upgradeCategoryMapper.toDtoList(categories);
    }

    @Transactional(readOnly = true)
    public List<UpgradeCategoryDto> getActiveUpgradeCategories() {
        List<UpgradeCategory> categories = findActiveCategories();
        return upgradeCategoryMapper.toDtoList(categories);
    }

    @Transactional(readOnly = true)
    public List<UpgradeCategoryDto> searchUpgradeCategories(String searchTerm) {
        List<UpgradeCategory> categories = searchCategories(searchTerm);
        return upgradeCategoryMapper.toDtoList(categories);
    }

    public UpgradeCategory createCategory(String name, String description, Boolean isActive) {
        log.info("Creating new upgrade category with name: {}", name);
        
        validateCategoryCreation(name);
        
        Integer maxSortOrder = upgradeCategoryRepository.findMaxSortOrder();
        Integer sortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;
        
        String key = name.toLowerCase().replaceAll("[^a-z0-9]", "_").replaceAll("_+", "_");
        if (key.endsWith("_")) {
            key = key.substring(0, key.length() - 1);
        }
        
        UpgradeCategory category = UpgradeCategory.builder()
                .name(name)
                .key(key)
                .description(description)
                .isActive(isActive != null ? isActive : true)
                .sortOrder(sortOrder)
                .build();

        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        log.info("Successfully created upgrade category with id: {}", savedCategory.getId());
        return savedCategory;
    }

    public UpgradeCategoryDto createUpgradeCategory(UpgradeCategoryCreateDto createDto) {
        log.info("Creating new upgrade category with name: {}", createDto.getName());
        
        validateCategoryCreation(createDto.getName());
        
        Integer maxSortOrder = upgradeCategoryRepository.findMaxSortOrder();
        Integer sortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;
        
        String key = createDto.getName().toLowerCase().replaceAll("[^a-z0-9]", "_").replaceAll("_+", "_");
        if (key.endsWith("_")) {
            key = key.substring(0, key.length() - 1);
        }
        
        UpgradeCategory category = upgradeCategoryMapper.toEntity(createDto);
        category.setKey(key);
        category.setSortOrder(sortOrder);
        
        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        log.info("Successfully created upgrade category with id: {}", savedCategory.getId());
        return upgradeCategoryMapper.toDto(savedCategory);
    }

    public UpgradeCategory updateCategory(Integer categoryId, String name, String description, Boolean isActive) {
        log.info("Updating upgrade category with id: {}", categoryId);
        
        UpgradeCategory category = findById(categoryId);
        
        if (name != null && !name.equals(category.getName())) {
            validateCategoryNameUniqueness(name, categoryId);
            category.setName(name);
        }
        
        if (description != null) {
            category.setDescription(description);
        }
        
        if (isActive != null) {
            category.setIsActive(isActive);
        }

        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        log.info("Successfully updated upgrade category with id: {}", savedCategory.getId());
        return savedCategory;
    }

    public UpgradeCategoryDto updateUpgradeCategory(Integer categoryId, UpgradeCategoryUpdateDto updateDto) {
        log.info("Updating upgrade category with id: {}", categoryId);
        
        UpgradeCategory category = findById(categoryId);
        
        if (updateDto.getName() != null && !updateDto.getName().equals(category.getName())) {
            validateCategoryNameUniqueness(updateDto.getName(), categoryId);
        }
        
        upgradeCategoryMapper.updateEntity(category, updateDto);
        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        
        log.info("Successfully updated upgrade category with id: {}", savedCategory.getId());
        return upgradeCategoryMapper.toDto(savedCategory);
    }

    public UpgradeCategory updateCategorySortOrder(Integer categoryId, Integer sortOrder) {
        log.info("Updating sort order for upgrade category with id: {} to {}", categoryId, sortOrder);
        
        UpgradeCategory category = findById(categoryId);
        category.setSortOrder(sortOrder);
        
        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        log.info("Successfully updated sort order for upgrade category with id: {}", savedCategory.getId());
        return savedCategory;
    }

    public UpgradeCategory activateCategory(Integer categoryId) {
        log.info("Activating upgrade category with id: {}", categoryId);
        
        UpgradeCategory category = findById(categoryId);
        category.setIsActive(true);
        
        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        log.info("Successfully activated upgrade category with id: {}", savedCategory.getId());
        return savedCategory;
    }

    public UpgradeCategory deactivateCategory(Integer categoryId) {
        log.info("Deactivating upgrade category with id: {}", categoryId);
        
        UpgradeCategory category = findById(categoryId);
        category.setIsActive(false);
        
        UpgradeCategory savedCategory = upgradeCategoryRepository.save(category);
        log.info("Successfully deactivated upgrade category with id: {}", savedCategory.getId());
        return savedCategory;
    }

    public void deleteCategory(Integer categoryId) {
        log.info("Deleting upgrade category with id: {}", categoryId);
        
        UpgradeCategory category = findById(categoryId);
        
        long upgradeCount = countUpgradesByCategory(categoryId);
        if (upgradeCount > 0) {
            throw new InvalidStateException(
                String.format("Cannot delete category '%s' - it is used by %d upgrade(s)", 
                    category.getName(), upgradeCount)
            );
        }
        
        upgradeCategoryRepository.delete(category);
        log.info("Successfully deleted upgrade category with id: {}", categoryId);
    }

    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String name) {
        return !upgradeCategoryRepository.existsByName(name);
    }

    public void ensureDefaultCategories() {
        log.info("Ensuring default upgrade categories exist");
        
        String[] defaultCategories = {
            "Engine",
            "Suspension", 
            "Brakes",
            "Wheels & Tires",
            "Exhaust",
            "Interior",
            "Exterior",
            "Electronics",
            "Maintenance"
        };
        
        for (String categoryName : defaultCategories) {
            if (findByName(categoryName).isEmpty()) {
                createCategory(categoryName, "Default " + categoryName.toLowerCase() + " category", true);
            }
        }
        
        log.info("Default upgrade categories ensured");
    }

    private void validateCategoryCreation(String name) {
        if (upgradeCategoryRepository.existsByName(name)) {
            throw new DuplicateResourceException("UpgradeCategory", "name", name);
        }
    }

    private void validateCategoryNameUniqueness(String name, Integer categoryId) {
        if (upgradeCategoryRepository.existsByNameAndIdNot(name, categoryId)) {
            throw new DuplicateResourceException("UpgradeCategory", "name", name);
        }
    }
}