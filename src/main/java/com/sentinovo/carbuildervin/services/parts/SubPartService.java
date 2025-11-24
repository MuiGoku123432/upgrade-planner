package com.sentinovo.carbuildervin.services.parts;

import com.sentinovo.carbuildervin.entities.parts.Part;
import com.sentinovo.carbuildervin.entities.parts.PartCategory;
import com.sentinovo.carbuildervin.entities.parts.PartTier;
import com.sentinovo.carbuildervin.entities.parts.SubPart;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.exception.UnauthorizedException;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.repository.parts.SubPartRepository;
import com.sentinovo.carbuildervin.services.user.AuthenticationService;
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
public class SubPartService {

    private final SubPartRepository subPartRepository;
    private final PartService partService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;
    private final AuthenticationService authenticationService;

    @Transactional(readOnly = true)
    public SubPart findById(UUID id) {
        return subPartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubPart", id));
    }

    @Transactional(readOnly = true)
    public SubPart findByIdAndValidateOwnership(UUID id) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        return subPartRepository.findByIdAndOwnerId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("SubPart", id));
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByParentPartId(UUID partId) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.findByParentPartId(partId);
    }

    @Transactional(readOnly = true)
    public Page<SubPart> findByParentPartId(UUID partId, Pageable pageable) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.findByParentPartId(partId, pageable);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByParentPartIdOrderedBySortOrder(UUID partId) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.findByParentPartIdOrderBySortOrder(partId);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findUserSubParts(UUID userId) {
        validateUserAccess(userId);
        return subPartRepository.findByVehicleOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public Page<SubPart> findUserSubParts(UUID userId, Pageable pageable) {
        validateUserAccess(userId);
        return subPartRepository.findByVehicleOwnerId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findUserSubPartsByPriority(UUID userId) {
        validateUserAccess(userId);
        return subPartRepository.findByOwnerIdOrderByPriority(userId);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByUpgradeId(UUID upgradeId) {
        return subPartRepository.findByUpgradeId(upgradeId);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByVehicleId(UUID vehicleId) {
        return subPartRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByStatus(String status) {
        return subPartRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findUserSubPartsByStatus(UUID userId, String status) {
        validateUserAccess(userId);
        return subPartRepository.findByOwnerIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByParentPartIdAndStatus(UUID partId, String status) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.findByParentPartIdAndStatus(partId, status);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByPriorityValue(Integer priority) {
        return subPartRepository.findByPriorityValue(priority);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findHighPrioritySubParts(Integer minPriority) {
        return subPartRepository.findHighPrioritySubParts(minPriority);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findUserHighPrioritySubParts(UUID userId, Integer minPriority) {
        validateUserAccess(userId);
        return subPartRepository.findByOwnerIdAndMinPriority(userId, minPriority);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findRequiredSubParts() {
        return subPartRepository.findRequiredSubParts();
    }

    @Transactional(readOnly = true)
    public List<SubPart> findRequiredSubPartsByParent(UUID partId) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.findRequiredSubPartsByParentPartId(partId);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return subPartRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findUserSubPartsByPriceRange(UUID userId, BigDecimal minPrice, BigDecimal maxPrice) {
        validateUserAccess(userId);
        return subPartRepository.findByOwnerIdAndPriceBetween(userId, minPrice, maxPrice);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findByTargetDateRange(LocalDate startDate, LocalDate endDate) {
        return subPartRepository.findByTargetPurchaseDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<SubPart> findOverdueSubParts(LocalDate asOfDate) {
        return subPartRepository.findOverdueSubParts(asOfDate);
    }

    @Transactional(readOnly = true)
    public List<SubPart> searchUserSubParts(UUID userId, String searchTerm) {
        validateUserAccess(userId);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findUserSubParts(userId);
        }
        return subPartRepository.searchByOwnerIdAndTerm(userId, searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public List<SubPart> searchSubPartsByParent(UUID userId, UUID partId, String searchTerm) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findByParentPartId(partId);
        }
        return subPartRepository.searchByParentPartAndTerm(userId, partId, searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public Page<SubPart> findUserSubPartsWithFilters(UUID userId, String categoryCode, String tierCode, 
                                                    String status, Integer minPriority, Boolean requiredOnly, 
                                                    Pageable pageable) {
        validateUserAccess(userId);
        return subPartRepository.findByOwnerWithFilters(userId, categoryCode, tierCode, status, 
                                                       minPriority, requiredOnly, pageable);
    }

    public SubPart createSubPart(UUID parentPartId, String name, String brand, String categoryCode, String tierCode,
                                String productUrl, BigDecimal price, String currencyCode, Boolean isRequired,
                                String status, Integer priorityValue, LocalDate targetPurchaseDate, 
                                Integer sortOrder, String notes) {
        log.info("Creating new sub-part for parent part: {}", parentPartId);
        
        Part parentPart = partService.findByIdAndValidateOwnership(parentPartId);
        
        PartCategory category = null;
        if (categoryCode != null) {
            category = partCategoryService.findByCode(categoryCode);
        }
        
        PartTier tier = null;
        if (tierCode != null) {
            tier = partTierService.findByCode(tierCode);
        }
        
        SubPart subPart = SubPart.builder()
                .parentPart(parentPart)
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

        SubPart savedSubPart = subPartRepository.save(subPart);
        log.info("Successfully created sub-part with id: {} for parent part: {}", savedSubPart.getId(), parentPartId);
        return savedSubPart;
    }

    public SubPart updateSubPart(UUID subPartId, String name, String brand, String categoryCode, String tierCode,
                                String productUrl, BigDecimal price, String currencyCode, Boolean isRequired,
                                String status, Integer priorityValue, LocalDate targetPurchaseDate, 
                                Integer sortOrder, String notes) {
        log.info("Updating sub-part with id: {}", subPartId);
        
        SubPart subPart = findByIdAndValidateOwnership(subPartId);
        
        if (name != null) subPart.setName(name);
        if (brand != null) subPart.setBrand(brand);
        
        if (categoryCode != null) {
            PartCategory category = partCategoryService.findByCode(categoryCode);
            subPart.setPartCategory(category);
        }
        
        if (tierCode != null) {
            PartTier tier = partTierService.findByCode(tierCode);
            subPart.setPartTier(tier);
        }
        
        if (productUrl != null) subPart.setProductUrl(productUrl);
        if (price != null) subPart.setPrice(price);
        if (currencyCode != null) subPart.setCurrencyCode(currencyCode);
        if (isRequired != null) subPart.setIsRequired(isRequired);
        
        if (status != null) {
            validateStatusTransition(subPart.getStatus(), status);
            subPart.setStatus(status);
        }
        
        if (priorityValue != null) subPart.setPriorityValue(priorityValue);
        if (targetPurchaseDate != null) subPart.setTargetPurchaseDate(targetPurchaseDate);
        if (sortOrder != null) subPart.setSortOrder(sortOrder);
        if (notes != null) subPart.setNotes(notes);

        SubPart savedSubPart = subPartRepository.save(subPart);
        log.info("Successfully updated sub-part with id: {}", savedSubPart.getId());
        return savedSubPart;
    }

    public SubPart updateSubPartStatus(UUID subPartId, String status) {
        log.info("Updating status for sub-part with id: {} to {}", subPartId, status);
        
        SubPart subPart = findByIdAndValidateOwnership(subPartId);
        validateStatusTransition(subPart.getStatus(), status);
        subPart.setStatus(status);
        
        SubPart savedSubPart = subPartRepository.save(subPart);
        log.info("Successfully updated status for sub-part with id: {}", savedSubPart.getId());
        return savedSubPart;
    }

    public void deleteSubPart(UUID subPartId) {
        log.info("Deleting sub-part with id: {}", subPartId);
        
        SubPart subPart = findByIdAndValidateOwnership(subPartId);
        subPartRepository.delete(subPart);
        
        log.info("Successfully deleted sub-part with id: {}", subPartId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByParentPart(UUID partId) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        BigDecimal total = subPartRepository.calculateTotalCostByParentPartId(partId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByUpgrade(UUID upgradeId) {
        BigDecimal total = subPartRepository.calculateTotalCostByUpgradeId(upgradeId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByUser(UUID userId) {
        validateUserAccess(userId);
        BigDecimal total = subPartRepository.calculateTotalCostByOwnerId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countByParentPartId(UUID partId) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.countByParentPartId(partId);
    }

    @Transactional(readOnly = true)
    public long countByParentPartIdAndStatus(UUID partId, String status) {
        partService.findByIdAndValidateOwnership(partId); // Validate ownership
        return subPartRepository.countByParentPartIdAndStatus(partId, status);
    }

    @Transactional(readOnly = true)
    public long countUserSubPartsByStatus(UUID userId, String status) {
        validateUserAccess(userId);
        return subPartRepository.countByOwnerIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAveragePriceByCategory(String categoryCode) {
        BigDecimal avg = subPartRepository.calculateAveragePriceByCategoryCode(categoryCode);
        return avg != null ? avg : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAveragePriceByTier(String tierCode) {
        BigDecimal avg = subPartRepository.calculateAveragePriceByTierCode(tierCode);
        return avg != null ? avg : BigDecimal.ZERO;
    }

    private void validateUserAccess(UUID userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new UnauthorizedException("access", "sub-parts", userId);
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
                String.format("Can only change CANCELLED sub-parts back to PLANNED, not %s", newStatus)
            );
        }
    }
}