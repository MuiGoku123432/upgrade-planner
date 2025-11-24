package com.sentinovo.carbuildervin.services.parts;

import com.sentinovo.carbuildervin.entities.parts.Part;
import com.sentinovo.carbuildervin.entities.parts.PartCategory;
import com.sentinovo.carbuildervin.entities.parts.PartTier;
import com.sentinovo.carbuildervin.entities.vehicle.VehicleUpgrade;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.exception.UnauthorizedException;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.repository.parts.PartRepository;
import com.sentinovo.carbuildervin.services.user.AuthenticationService;
import com.sentinovo.carbuildervin.services.vehicle.VehicleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PartService {

    private final PartRepository partRepository;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;
    private final AuthenticationService authenticationService;

    @Transactional(readOnly = true)
    public Part findById(UUID id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part", id));
    }

    @Transactional(readOnly = true)
    public Part findByIdAndValidateOwnership(UUID id) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        return partRepository.findByIdAndOwnerId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Part", id));
    }

    @Transactional(readOnly = true)
    public Part findByIdWithSubParts(UUID id) {
        Part part = findByIdAndValidateOwnership(id);
        return partRepository.findByIdWithSubParts(id).orElse(part);
    }

    @Transactional(readOnly = true)
    public List<Part> findByUpgradeId(UUID upgradeId) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.findByVehicleUpgradeId(upgradeId);
    }

    @Transactional(readOnly = true)
    public Page<Part> findByUpgradeId(UUID upgradeId, Pageable pageable) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.findByVehicleUpgradeId(upgradeId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Part> findByUpgradeIdWithSubParts(UUID upgradeId) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.findByUpgradeIdWithSubParts(upgradeId);
    }

    @Transactional(readOnly = true)
    public List<Part> findByUpgradeIdOrderedBySortOrder(UUID upgradeId) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.findByUpgradeIdOrderBySortOrder(upgradeId);
    }

    @Transactional(readOnly = true)
    public List<Part> findUserParts(UUID userId) {
        validateUserAccess(userId);
        return partRepository.findByVehicleOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Part> findUserParts(UUID userId, Pageable pageable) {
        validateUserAccess(userId);
        return partRepository.findByVehicleOwnerId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Part> findUserPartsByPriority(UUID userId) {
        validateUserAccess(userId);
        return partRepository.findByOwnerIdOrderByPriority(userId);
    }

    @Transactional(readOnly = true)
    public List<Part> findByStatus(String status) {
        return partRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Part> findUserPartsByStatus(UUID userId, String status) {
        validateUserAccess(userId);
        return partRepository.findByOwnerIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public List<Part> findByUpgradeIdAndStatus(UUID upgradeId, String status) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.findByUpgradeIdAndStatus(upgradeId, status);
    }

    @Transactional(readOnly = true)
    public List<Part> findByPriorityValue(Integer priority) {
        return partRepository.findByPriorityValue(priority);
    }

    @Transactional(readOnly = true)
    public List<Part> findHighPriorityParts(Integer minPriority) {
        return partRepository.findHighPriorityParts(minPriority);
    }

    @Transactional(readOnly = true)
    public List<Part> findUserHighPriorityParts(UUID userId, Integer minPriority) {
        validateUserAccess(userId);
        return partRepository.findByOwnerIdAndMinPriority(userId, minPriority);
    }

    @Transactional(readOnly = true)
    public List<Part> findRequiredParts() {
        return partRepository.findRequiredParts();
    }

    @Transactional(readOnly = true)
    public List<Part> findRequiredPartsByUpgrade(UUID upgradeId) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.findRequiredPartsByUpgradeId(upgradeId);
    }

    @Transactional(readOnly = true)
    public List<Part> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return partRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Transactional(readOnly = true)
    public List<Part> findUserPartsByPriceRange(UUID userId, BigDecimal minPrice, BigDecimal maxPrice) {
        validateUserAccess(userId);
        return partRepository.findByOwnerIdAndPriceBetween(userId, minPrice, maxPrice);
    }

    @Transactional(readOnly = true)
    public List<Part> findByTargetDateRange(LocalDate startDate, LocalDate endDate) {
        return partRepository.findByTargetPurchaseDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Part> findOverdueParts(LocalDate asOfDate) {
        return partRepository.findOverdueParts(asOfDate);
    }

    @Transactional(readOnly = true)
    public List<Part> searchUserParts(UUID userId, String searchTerm) {
        validateUserAccess(userId);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findUserParts(userId);
        }
        return partRepository.searchByOwnerIdAndTerm(userId, searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public Page<Part> findUserPartsWithFilters(UUID userId, String categoryCode, String tierCode, 
                                              String status, Integer minPriority, Boolean requiredOnly, 
                                              Pageable pageable) {
        validateUserAccess(userId);
        return partRepository.findByOwnerWithFilters(userId, categoryCode, tierCode, status, 
                                                    minPriority, requiredOnly, pageable);
    }

    public Part createPart(UUID upgradeId, String name, String brand, String categoryCode, String tierCode,
                          String productUrl, BigDecimal price, String currencyCode, Boolean isRequired,
                          String status, Integer priorityValue, LocalDate targetPurchaseDate, 
                          Integer sortOrder, String notes) {
        log.info("Creating new part for upgrade: {}", upgradeId);
        
        VehicleUpgrade upgrade = vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId);
        
        PartCategory category = null;
        if (categoryCode != null) {
            category = partCategoryService.findByCode(categoryCode);
        }
        
        PartTier tier = null;
        if (tierCode != null) {
            tier = partTierService.findByCode(tierCode);
        }
        
        Part part = Part.builder()
                .vehicleUpgrade(upgrade)
                .name(name)
                .brand(brand)
                .partCategory(category)
                .partTier(tier)
                .productUrl(productUrl)
                .price(price)
                .currencyCode(currencyCode != null ? currencyCode : "USD")
                .isRequired(isRequired != null ? isRequired : true)
                .status(status != null ? status : "PLANNED")
                .priorityValue(priorityValue != null ? priorityValue : 5)
                .targetPurchaseDate(targetPurchaseDate)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .notes(notes)
                .build();

        Part savedPart = partRepository.save(part);
        log.info("Successfully created part with id: {} for upgrade: {}", savedPart.getId(), upgradeId);
        return savedPart;
    }

    public Part updatePart(UUID partId, String name, String brand, String categoryCode, String tierCode,
                          String productUrl, BigDecimal price, String currencyCode, Boolean isRequired,
                          String status, Integer priorityValue, LocalDate targetPurchaseDate, 
                          Integer sortOrder, String notes) {
        log.info("Updating part with id: {}", partId);
        
        Part part = findByIdAndValidateOwnership(partId);
        
        if (name != null) part.setName(name);
        if (brand != null) part.setBrand(brand);
        
        if (categoryCode != null) {
            PartCategory category = partCategoryService.findByCode(categoryCode);
            part.setPartCategory(category);
        }
        
        if (tierCode != null) {
            PartTier tier = partTierService.findByCode(tierCode);
            part.setPartTier(tier);
        }
        
        if (productUrl != null) part.setProductUrl(productUrl);
        if (price != null) part.setPrice(price);
        if (currencyCode != null) part.setCurrencyCode(currencyCode);
        if (isRequired != null) part.setIsRequired(isRequired);
        
        if (status != null) {
            validateStatusTransition(part.getStatus(), status);
            part.setStatus(status);
        }
        
        if (priorityValue != null) part.setPriorityValue(priorityValue);
        if (targetPurchaseDate != null) part.setTargetPurchaseDate(targetPurchaseDate);
        if (sortOrder != null) part.setSortOrder(sortOrder);
        if (notes != null) part.setNotes(notes);

        Part savedPart = partRepository.save(part);
        log.info("Successfully updated part with id: {}", savedPart.getId());
        return savedPart;
    }

    public Part updatePartStatus(UUID partId, String status) {
        log.info("Updating status for part with id: {} to {}", partId, status);
        
        Part part = findByIdAndValidateOwnership(partId);
        validateStatusTransition(part.getStatus(), status);
        part.setStatus(status);
        
        Part savedPart = partRepository.save(part);
        log.info("Successfully updated status for part with id: {}", savedPart.getId());
        return savedPart;
    }

    public void deletePart(UUID partId) {
        log.info("Deleting part with id: {}", partId);
        
        Part part = findByIdAndValidateOwnership(partId);
        partRepository.delete(part);
        
        log.info("Successfully deleted part with id: {}", partId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByUpgrade(UUID upgradeId) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        BigDecimal total = partRepository.calculateTotalCostByUpgradeId(upgradeId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByUser(UUID userId) {
        validateUserAccess(userId);
        BigDecimal total = partRepository.calculateTotalCostByOwnerId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByVehicle(UUID vehicleId) {
        BigDecimal total = partRepository.calculateTotalCostByVehicleId(vehicleId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countByUpgradeId(UUID upgradeId) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.countByUpgradeId(upgradeId);
    }

    @Transactional(readOnly = true)
    public long countByUpgradeIdAndStatus(UUID upgradeId, String status) {
        vehicleUpgradeService.findByIdAndValidateOwnership(upgradeId); // Validate ownership
        return partRepository.countByUpgradeIdAndStatus(upgradeId, status);
    }

    @Transactional(readOnly = true)
    public long countUserPartsByStatus(UUID userId, String status) {
        validateUserAccess(userId);
        return partRepository.countByOwnerIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAveragePriceByCategory(String categoryCode) {
        BigDecimal avg = partRepository.calculateAveragePriceByCategoryCode(categoryCode);
        return avg != null ? avg : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAveragePriceByTier(String tierCode) {
        BigDecimal avg = partRepository.calculateAveragePriceByTierCode(tierCode);
        return avg != null ? avg : BigDecimal.ZERO;
    }

    private void validateUserAccess(UUID userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new UnauthorizedException("access", "parts", userId);
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        if ("INSTALLED".equals(currentStatus) && !"INSTALLED".equals(newStatus)) {
            throw new ValidationException(
                String.format("Cannot change status from INSTALLED to %s", newStatus)
            );
        }
        
        if ("CANCELLED".equals(currentStatus) && !"PLANNED".equals(newStatus)) {
            throw new ValidationException(
                String.format("Can only change CANCELLED parts back to PLANNED, not %s", newStatus)
            );
        }
    }
}