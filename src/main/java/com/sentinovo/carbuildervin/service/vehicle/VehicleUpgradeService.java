package com.sentinovo.carbuildervin.service.vehicle;

import com.sentinovo.carbuildervin.dto.build.*;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.entities.vehicle.UpgradeCategory;
import com.sentinovo.carbuildervin.entities.vehicle.Vehicle;
import com.sentinovo.carbuildervin.entities.vehicle.VehicleUpgrade;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.exception.UnauthorizedException;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.mapper.vehicle.VehicleUpgradeMapper;
import com.sentinovo.carbuildervin.repository.vehicle.VehicleUpgradeRepository;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VehicleUpgradeService {

    private final VehicleUpgradeRepository vehicleUpgradeRepository;
    private final VehicleService vehicleService;
    private final UpgradeCategoryService upgradeCategoryService;
    private final AuthenticationService authenticationService;
    private final VehicleUpgradeMapper vehicleUpgradeMapper;

    @Transactional(readOnly = true)
    public VehicleUpgrade findById(UUID id) {
        return vehicleUpgradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleUpgrade", id));
    }

    @Transactional(readOnly = true)
    public VehicleUpgrade findByIdAndValidateOwnership(UUID id) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        return vehicleUpgradeRepository.findByIdAndOwnerId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleUpgrade", id));
    }

    @Transactional(readOnly = true)
    public VehicleUpgrade findByIdWithParts(UUID id) {
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(id);
        return vehicleUpgradeRepository.findByIdWithParts(id).orElse(upgrade);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findByVehicleId(UUID vehicleId) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public Page<VehicleUpgrade> findByVehicleId(UUID vehicleId, Pageable pageable) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.findByVehicleId(vehicleId, pageable);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findByVehicleIdWithParts(UUID vehicleId) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.findByVehicleIdWithParts(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findUserUpgrades(UUID userId) {
        validateUserAccess(userId);
        return vehicleUpgradeRepository.findByVehicleOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public Page<VehicleUpgrade> findUserUpgrades(UUID userId, Pageable pageable) {
        validateUserAccess(userId);
        return vehicleUpgradeRepository.findByVehicleOwnerId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findByStatus(String status) {
        return vehicleUpgradeRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findUserUpgradesByStatus(UUID userId, String status) {
        validateUserAccess(userId);
        return vehicleUpgradeRepository.findByOwnerIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findByPriorityLevel(Integer priorityLevel) {
        return vehicleUpgradeRepository.findByPriorityLevel(priorityLevel);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findHighPriorityUpgrades(UUID userId, Integer minPriority) {
        validateUserAccess(userId);
        return vehicleUpgradeRepository.findByOwnerIdAndMinPriority(userId, minPriority);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findPrimaryUpgrades() {
        return vehicleUpgradeRepository.findPrimaryUpgrades();
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findPrimaryUpgradesByVehicle(UUID vehicleId) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.findPrimaryUpgradesByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public Optional<VehicleUpgrade> findPrimaryUpgradeByVehicleAndCategory(UUID vehicleId, Integer categoryId) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.findPrimaryUpgradeByVehicleAndCategory(vehicleId, categoryId);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findByTargetDateRange(LocalDate startDate, LocalDate endDate) {
        return vehicleUpgradeRepository.findByTargetCompletionDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> findOverdueUpgrades(LocalDate asOfDate) {
        return vehicleUpgradeRepository.findOverdueUpgrades(asOfDate);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgrade> searchUserUpgrades(UUID userId, String searchTerm) {
        validateUserAccess(userId);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findUserUpgrades(userId);
        }
        return vehicleUpgradeRepository.searchByOwnerIdAndTerm(userId, searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public Optional<VehicleUpgrade> findBySlug(String slug) {
        return vehicleUpgradeRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public Optional<VehicleUpgrade> findByVehicleAndSlug(UUID vehicleId, String slug) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.findByVehicleIdAndSlug(vehicleId, slug);
    }

    // ===== DTO-Based Methods =====

    @Transactional(readOnly = true)
    public VehicleUpgradeDto getVehicleUpgradeById(UUID id) {
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(id);
        return vehicleUpgradeMapper.toDto(upgrade);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgradeDto> getVehicleUpgradesByVehicleId(UUID vehicleId) {
        List<VehicleUpgrade> upgrades = findByVehicleId(vehicleId);
        return vehicleUpgradeMapper.toDtoList(upgrades);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<VehicleUpgradeDto> getVehicleUpgradesByVehicleIdPaged(UUID vehicleId, Pageable pageable) {
        Page<VehicleUpgrade> page = findByVehicleId(vehicleId, pageable);
        return vehicleUpgradeMapper.toPageDto(page);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgradeSummaryDto> getVehicleUpgradeSummariesByVehicleId(UUID vehicleId) {
        List<VehicleUpgrade> upgrades = findByVehicleId(vehicleId);
        return upgrades.stream()
                .map(vehicleUpgradeMapper::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgradeDto> getUserVehicleUpgrades(UUID userId) {
        List<VehicleUpgrade> upgrades = findUserUpgrades(userId);
        return vehicleUpgradeMapper.toDtoList(upgrades);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<VehicleUpgradeDto> getUserVehicleUpgradesPaged(UUID userId, Pageable pageable) {
        Page<VehicleUpgrade> page = findUserUpgrades(userId, pageable);
        return vehicleUpgradeMapper.toPageDto(page);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgradeDto> getUserVehicleUpgradesByStatus(UUID userId, String status) {
        List<VehicleUpgrade> upgrades = findUserUpgradesByStatus(userId, status);
        return vehicleUpgradeMapper.toDtoList(upgrades);
    }

    @Transactional(readOnly = true)
    public List<VehicleUpgradeDto> searchUserVehicleUpgradesByTerm(UUID userId, String searchTerm) {
        List<VehicleUpgrade> upgrades = searchUserUpgrades(userId, searchTerm);
        return vehicleUpgradeMapper.toDtoList(upgrades);
    }

    public VehicleUpgrade createUpgrade(UUID vehicleId, Integer categoryId, String name, String description,
                                       Integer priorityLevel, LocalDate targetCompletionDate, Boolean isPrimary) {
        log.info("Creating new upgrade for vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleService.findByIdAndValidateOwnership(vehicleId);
        UpgradeCategory category = upgradeCategoryService.findById(categoryId);
        
        if (isPrimary != null && isPrimary) {
            validatePrimaryUpgrade(vehicleId, categoryId);
        }
        
        VehicleUpgrade upgrade = VehicleUpgrade.builder()
                .vehicle(vehicle)
                .upgradeCategory(category)
                .name(name)
                .description(description)
                .priorityLevel(priorityLevel != null ? priorityLevel : 1)
                .targetCompletionDate(targetCompletionDate)
                .status("PLANNED")
                .isPrimaryForCategory(isPrimary != null ? isPrimary : false)
                .build();

        VehicleUpgrade savedUpgrade = vehicleUpgradeRepository.save(upgrade);
        log.info("Successfully created upgrade with id: {} for vehicle: {}", savedUpgrade.getId(), vehicleId);
        return savedUpgrade;
    }

    public VehicleUpgradeDto createVehicleUpgrade(UUID vehicleId, VehicleUpgradeCreateDto createDto) {
        log.info("Creating new upgrade for vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleService.findByIdAndValidateOwnership(vehicleId);
        UpgradeCategory category = upgradeCategoryService.findById(createDto.getCategoryId());
        
        if (createDto.getIsPrimaryForCategory() != null && createDto.getIsPrimaryForCategory()) {
            validatePrimaryUpgrade(vehicleId, createDto.getCategoryId());
        }
        
        VehicleUpgrade upgrade = vehicleUpgradeMapper.toEntity(createDto);
        upgrade.setVehicle(vehicle);
        upgrade.setUpgradeCategory(category);
        
        VehicleUpgrade savedUpgrade = vehicleUpgradeRepository.save(upgrade);
        log.info("Successfully created upgrade with id: {} for vehicle: {}", savedUpgrade.getId(), vehicleId);
        return vehicleUpgradeMapper.toDto(savedUpgrade);
    }

    public VehicleUpgrade updateUpgrade(UUID upgradeId, String name, String description, Integer priorityLevel,
                                       LocalDate targetCompletionDate, String status, Boolean isPrimary) {
        log.info("Updating upgrade with id: {}", upgradeId);
        
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(upgradeId);
        
        if (name != null) upgrade.setName(name);
        if (description != null) upgrade.setDescription(description);
        if (priorityLevel != null) upgrade.setPriorityLevel(priorityLevel);
        if (targetCompletionDate != null) upgrade.setTargetCompletionDate(targetCompletionDate);
        if (status != null) {
            validateStatusTransition(upgrade.getStatus(), status);
            upgrade.setStatus(status);
        }
        if (isPrimary != null) {
            if (isPrimary && !upgrade.getIsPrimaryForCategory()) {
                validatePrimaryUpgrade(upgrade.getVehicle().getId(), upgrade.getUpgradeCategory().getId());
            }
            upgrade.setIsPrimaryForCategory(isPrimary);
        }

        VehicleUpgrade savedUpgrade = vehicleUpgradeRepository.save(upgrade);
        log.info("Successfully updated upgrade with id: {}", savedUpgrade.getId());
        return savedUpgrade;
    }

    public VehicleUpgradeDto updateVehicleUpgrade(UUID upgradeId, VehicleUpgradeUpdateDto updateDto) {
        log.info("Updating upgrade with id: {}", upgradeId);
        
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(upgradeId);
        
        // Handle category update
        if (updateDto.getCategoryId() != null) {
            UpgradeCategory category = upgradeCategoryService.findById(updateDto.getCategoryId());
            upgrade.setUpgradeCategory(category);
        }
        
        // Handle status transition validation
        if (updateDto.getStatus() != null) {
            validateStatusTransition(upgrade.getStatus(), updateDto.getStatus());
        }
        
        // Handle primary upgrade validation
        if (updateDto.getIsPrimaryForCategory() != null) {
            if (updateDto.getIsPrimaryForCategory() && !upgrade.getIsPrimaryForCategory()) {
                validatePrimaryUpgrade(upgrade.getVehicle().getId(), upgrade.getUpgradeCategory().getId());
            }
        }
        
        vehicleUpgradeMapper.updateEntity(upgrade, updateDto);
        VehicleUpgrade savedUpgrade = vehicleUpgradeRepository.save(upgrade);
        
        log.info("Successfully updated upgrade with id: {}", savedUpgrade.getId());
        return vehicleUpgradeMapper.toDto(savedUpgrade);
    }

    public VehicleUpgrade updateUpgradeStatus(UUID upgradeId, String status) {
        log.info("Updating status for upgrade with id: {} to {}", upgradeId, status);
        
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(upgradeId);
        validateStatusTransition(upgrade.getStatus(), status);
        upgrade.setStatus(status);
        
        VehicleUpgrade savedUpgrade = vehicleUpgradeRepository.save(upgrade);
        log.info("Successfully updated status for upgrade with id: {}", savedUpgrade.getId());
        return savedUpgrade;
    }

    public VehicleUpgradeDto updateVehicleUpgradeStatusDto(UUID upgradeId, String status) {
        log.info("Updating status for upgrade with id: {} to {}", upgradeId, status);
        
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(upgradeId);
        validateStatusTransition(upgrade.getStatus(), status);
        upgrade.setStatus(status);
        
        VehicleUpgrade savedUpgrade = vehicleUpgradeRepository.save(upgrade);
        log.info("Successfully updated status for upgrade with id: {}", savedUpgrade.getId());
        return vehicleUpgradeMapper.toDto(savedUpgrade);
    }

    public void deleteUpgrade(UUID upgradeId) {
        log.info("Deleting upgrade with id: {}", upgradeId);
        
        VehicleUpgrade upgrade = findByIdAndValidateOwnership(upgradeId);
        vehicleUpgradeRepository.delete(upgrade);
        
        log.info("Successfully deleted upgrade with id: {}", upgradeId);
    }

    @Transactional(readOnly = true)
    public long countByVehicleId(UUID vehicleId) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.countByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public long countByVehicleIdAndStatus(UUID vehicleId, String status) {
        vehicleService.findByIdAndValidateOwnership(vehicleId); // Validate ownership
        return vehicleUpgradeRepository.countByVehicleIdAndStatus(vehicleId, status);
    }

    @Transactional(readOnly = true)
    public long countUserUpgradesByStatus(UUID userId, String status) {
        validateUserAccess(userId);
        return vehicleUpgradeRepository.countByOwnerIdAndStatus(userId, status);
    }

    private void validateUserAccess(UUID userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new UnauthorizedException("access", "vehicle upgrades", userId);
        }
    }

    private void validatePrimaryUpgrade(UUID vehicleId, Integer categoryId) {
        Optional<VehicleUpgrade> existingPrimary = findPrimaryUpgradeByVehicleAndCategory(vehicleId, categoryId);
        if (existingPrimary.isPresent()) {
            throw new ValidationException(
                String.format("A primary upgrade already exists for this category in this vehicle")
            );
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        if ("COMPLETED".equals(currentStatus) && !"COMPLETED".equals(newStatus)) {
            throw new ValidationException(
                String.format("Cannot change status from COMPLETED to %s", newStatus)
            );
        }
        
        if ("CANCELLED".equals(currentStatus) && !"PLANNED".equals(newStatus)) {
            throw new ValidationException(
                String.format("Can only change CANCELLED upgrades back to PLANNED, not %s", newStatus)
            );
        }
    }
}