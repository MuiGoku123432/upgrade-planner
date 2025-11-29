package com.sentinovo.carbuildervin.service.vehicle;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.dto.vehicle.*;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.entities.vehicle.Vehicle;
import com.sentinovo.carbuildervin.exception.DuplicateResourceException;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.exception.UnauthorizedException;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.mapper.vehicle.VehicleMapper;
import com.sentinovo.carbuildervin.repository.vehicle.VehicleRepository;
import com.sentinovo.carbuildervin.service.external.VinDecodingService;
import com.sentinovo.carbuildervin.service.external.VinDecodingService.VinDecodingResponse;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import com.sentinovo.carbuildervin.service.user.UserService;
import com.sentinovo.carbuildervin.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final VehicleMapper vehicleMapper;
    private final VinDecodingService vinDecodingService;

    @Transactional(readOnly = true)
    public Vehicle findById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    @Transactional(readOnly = true)
    public VehicleDto getVehicleById(UUID id) {
        Vehicle vehicle = findByIdAndValidateOwnership(id);
        return vehicleMapper.toDto(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle findByIdAndValidateOwnership(UUID id) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        return vehicleRepository.findByIdAndOwnerIdAndNotDeleted(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findUserVehicles(UUID userId) {
        validateUserAccess(userId);
        return vehicleRepository.findByOwnerIdAndNotDeleted(userId);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getUserVehicles(UUID userId) {
        validateUserAccess(userId);
        List<Vehicle> vehicles = vehicleRepository.findByOwnerIdAndNotDeleted(userId);
        return vehicleMapper.toDtoList(vehicles);
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> findUserVehicles(UUID userId, Pageable pageable) {
        validateUserAccess(userId);
        return vehicleRepository.findByOwnerIdAndNotDeleted(userId, pageable);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<VehicleDto> getUserVehiclesPaged(UUID userId, Pageable pageable) {
        validateUserAccess(userId);
        Page<Vehicle> page = vehicleRepository.findByOwnerIdAndNotDeleted(userId, pageable);
        return vehicleMapper.toPageDto(page);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findCurrentUserVehicles() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        return findUserVehicles(currentUserId);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getCurrentUserVehicles() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        return getUserVehicles(currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> searchUserVehicles(UUID userId, String searchTerm, Pageable pageable) {
        validateUserAccess(userId);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findUserVehicles(userId, pageable);
        }
        return vehicleRepository.searchByOwnerAndNotDeleted(userId, searchTerm.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<VehicleDto> searchUserVehiclesDto(UUID userId, String searchTerm, Pageable pageable) {
        validateUserAccess(userId);
        Page<Vehicle> page;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            page = findUserVehicles(userId, pageable);
        } else {
            page = vehicleRepository.searchByOwnerAndNotDeleted(userId, searchTerm.trim(), pageable);
        }
        return vehicleMapper.toPageDto(page);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findUserVehiclesWithUpgrades(UUID userId) {
        validateUserAccess(userId);
        return vehicleRepository.findByOwnerIdWithUpgrades(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Vehicle> findByVin(String vin) {
        if (vin == null || vin.trim().isEmpty()) {
            return Optional.empty();
        }
        return vehicleRepository.findByVinAndNotDeleted(vin.trim().toUpperCase());
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findProjectVehicles(UUID userId) {
        validateUserAccess(userId);
        return vehicleRepository.findProjectVehiclesByOwner(userId);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getProjectVehicles(UUID userId) {
        validateUserAccess(userId);
        List<Vehicle> vehicles = vehicleRepository.findProjectVehiclesByOwner(userId);
        return vehicleMapper.toDtoList(vehicles);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findRealVehicles(UUID userId) {
        validateUserAccess(userId);
        return vehicleRepository.findRealVehiclesByOwner(userId);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getRealVehicles(UUID userId) {
        validateUserAccess(userId);
        List<Vehicle> vehicles = vehicleRepository.findRealVehiclesByOwner(userId);
        return vehicleMapper.toDtoList(vehicles);
    }

    @Transactional(readOnly = true)
    public long countUserVehicles(UUID userId) {
        validateUserAccess(userId);
        return vehicleRepository.countByOwnerIdAndNotDeleted(userId);
    }

    public Vehicle createVehicle(UUID userId, String vin, Integer year, String make, String model, 
                                String trim, String nickname, String notes) {
        log.info("Creating new vehicle for user: {}", userId);
        
        validateUserAccess(userId);
        User owner = authenticationService.getCurrentUserOrThrow();
        
        if (vin != null) {
            validateVin(vin);
            vin = vin.trim().toUpperCase();
            validateVinUniqueness(vin);
        }
        
        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .vin(vin)
                .year(year)
                .make(make != null ? make.trim() : null)
                .model(model != null ? model.trim() : null)
                .trim(trim != null ? trim.trim() : null)
                .nickname(nickname != null ? nickname.trim() : null)
                .notes(notes)
                .isArchived(false)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully created vehicle with id: {} for user: {}", savedVehicle.getId(), userId);
        return savedVehicle;
    }

    public VehicleDto createVehicle(VehicleCreateDto createDto) {
        log.info("Creating new vehicle for current user");

        User owner = authenticationService.getCurrentUserOrThrow();

        String vin = createDto.getVin();
        if (vin != null && !vin.trim().isEmpty()) {
            validateVin(vin);
            vin = vin.trim().toUpperCase();
            validateVinUniqueness(vin);

            // Auto-populate from VIN if fields are missing
            boolean needsDecode = createDto.getYear() == null
                    || createDto.getMake() == null || createDto.getMake().trim().isEmpty()
                    || createDto.getModel() == null || createDto.getModel().trim().isEmpty();

            if (needsDecode) {
                try {
                    log.debug("Auto-populating vehicle details from VIN: {}", vin);
                    VinDecodingResponse decoded = vinDecodingService.decodeVin(vin);

                    // Only fill in missing fields (don't override user input)
                    if (createDto.getYear() == null) {
                        createDto.setYear(decoded.getYear());
                    }
                    if (createDto.getMake() == null || createDto.getMake().trim().isEmpty()) {
                        createDto.setMake(decoded.getMake());
                    }
                    if (createDto.getModel() == null || createDto.getModel().trim().isEmpty()) {
                        createDto.setModel(decoded.getModel());
                    }
                    if (createDto.getTrim() == null || createDto.getTrim().trim().isEmpty()) {
                        createDto.setTrim(decoded.getTrim());
                    }

                    log.info("VIN decode populated: {} {} {} {}",
                            decoded.getYear(), decoded.getMake(), decoded.getModel(), decoded.getTrim());
                } catch (Exception e) {
                    log.warn("VIN decode failed during vehicle creation, continuing with provided data: {}", e.getMessage());
                    // Don't fail vehicle creation if decode fails
                }
            }
        } else {
            vin = null;
        }

        Vehicle vehicle = vehicleMapper.toEntity(createDto);
        vehicle.setOwner(owner);
        vehicle.setVin(vin);
        vehicle.setIsArchived(false);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully created vehicle with id: {} for user: {}", savedVehicle.getId(), owner.getId());
        return vehicleMapper.toDto(savedVehicle);
    }

    public Vehicle createProjectVehicle(String nickname, String notes) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        log.info("Creating project vehicle for current user: {}", currentUserId);
        
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ValidationException("nickname", "Nickname is required for project vehicles");
        }
        
        return createVehicle(currentUserId, null, null, null, null, null, nickname, notes);
    }

    public VehicleDto createProjectVehicle(VehicleProjectCreateDto createDto) {
        log.info("Creating project vehicle for current user");
        
        User owner = authenticationService.getCurrentUserOrThrow();
        
        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .vin(null)
                .year(null)
                .make(null)
                .model(null)
                .trim(null)
                .nickname(createDto.getNickname())
                .notes(createDto.getNotes())
                .isArchived(false)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully created project vehicle with id: {} for user: {}", savedVehicle.getId(), owner.getId());
        return vehicleMapper.toDto(savedVehicle);
    }

    public Vehicle updateVehicle(UUID vehicleId, String vin, Integer year, String make, String model,
                                String trim, String nickname, String notes) {
        log.info("Updating vehicle with id: {}", vehicleId);
        
        Vehicle vehicle = findByIdAndValidateOwnership(vehicleId);
        
        if (vin != null && !vin.equals(vehicle.getVin())) {
            validateVin(vin);
            vin = vin.trim().toUpperCase();
            validateVinUniquenessForUpdate(vin, vehicleId);
            vehicle.setVin(vin);
        }
        
        if (year != null) vehicle.setYear(year);
        if (make != null) vehicle.setMake(make.trim());
        if (model != null) vehicle.setModel(model.trim());
        if (trim != null) vehicle.setTrim(trim.trim());
        if (nickname != null) vehicle.setNickname(nickname.trim());
        if (notes != null) vehicle.setNotes(notes);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully updated vehicle with id: {}", savedVehicle.getId());
        return savedVehicle;
    }

    public VehicleDto updateVehicle(UUID vehicleId, VehicleUpdateDto updateDto) {
        log.info("Updating vehicle with id: {}", vehicleId);
        
        Vehicle vehicle = findByIdAndValidateOwnership(vehicleId);
        
        String vin = updateDto.getVin();
        if (vin != null && !vin.equals(vehicle.getVin())) {
            validateVin(vin);
            vin = vin.trim().toUpperCase();
            validateVinUniquenessForUpdate(vin, vehicleId);
        }
        
        vehicleMapper.updateEntity(vehicle, updateDto);
        if (vin != null) {
            vehicle.setVin(vin);
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully updated vehicle with id: {}", savedVehicle.getId());
        return vehicleMapper.toDto(savedVehicle);
    }

    public Vehicle archiveVehicle(UUID vehicleId) {
        log.info("Archiving vehicle with id: {}", vehicleId);
        
        Vehicle vehicle = findByIdAndValidateOwnership(vehicleId);
        vehicle.setIsArchived(true);
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully archived vehicle with id: {}", savedVehicle.getId());
        return savedVehicle;
    }

    public VehicleDto archiveVehicleDto(UUID vehicleId) {
        Vehicle vehicle = archiveVehicle(vehicleId);
        return vehicleMapper.toDto(vehicle);
    }

    public Vehicle unarchiveVehicle(UUID vehicleId) {
        log.info("Unarchiving vehicle with id: {}", vehicleId);
        
        Vehicle vehicle = findById(vehicleId); // Allow unarchiving even if archived
        validateVehicleOwnership(vehicle);
        vehicle.setIsArchived(false);
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully unarchived vehicle with id: {}", savedVehicle.getId());
        return savedVehicle;
    }

    public VehicleDto unarchiveVehicleDto(UUID vehicleId) {
        Vehicle vehicle = unarchiveVehicle(vehicleId);
        return vehicleMapper.toDto(vehicle);
    }

    @Transactional(readOnly = true)
    public boolean isVinAvailable(String vin) {
        if (vin == null || vin.trim().isEmpty()) {
            return true;
        }
        return vehicleRepository.countByVinAndNotDeleted(vin.trim().toUpperCase()) == 0;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findRecentVehicles(LocalDateTime since) {
        return vehicleRepository.findRecentVehicles(since);
    }

    private void validateUserAccess(UUID userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new UnauthorizedException("access", "vehicles", userId);
        }
    }

    private void validateVehicleOwnership(Vehicle vehicle) {
        UUID currentUserId = authenticationService.getCurrentUserId();
        if (!vehicle.getOwner().getId().equals(currentUserId) && !authenticationService.isCurrentUserAdmin()) {
            throw new UnauthorizedException("access", "vehicle", vehicle.getId());
        }
    }

    private void validateVin(String vin) {
        if (vin == null || vin.trim().isEmpty()) {
            return; // VIN is optional
        }
        
        ValidationUtils.validateVin(vin);
    }

    private void validateVinUniqueness(String vin) {
        if (vin != null && !isVinAvailable(vin)) {
            throw new DuplicateResourceException("Vehicle", "VIN", vin);
        }
    }

    private void validateVinUniquenessForUpdate(String vin, UUID vehicleId) {
        if (vin != null && vehicleRepository.existsByVinAndIdNotAndNotDeleted(vin, vehicleId)) {
            throw new DuplicateResourceException("Vehicle", "VIN", vin);
        }
    }

    public PageResponseDto<VehicleDto> getVehiclesByOwnerUsername(
            String username, String vin, String make, String model, 
            Integer year, boolean includeArchived, Pageable pageable) {
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return getUserVehiclesPaged(user.getId(), pageable);
    }

    public VehicleDto getVehicleByIdAndOwnerUsername(UUID vehicleId, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        Vehicle vehicle = findById(vehicleId);
        
        if (!vehicle.getOwner().getId().equals(user.getId())) {
            throw new ValidationException("Access denied");
        }
        
        return vehicleMapper.toDto(vehicle);
    }

    public VehicleDto createVehicleForUsername(VehicleCreateDto createDto, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        Vehicle vehicle = vehicleMapper.toEntity(createDto);
        vehicle.setOwner(user);
        
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toDto(saved);
    }

    public void verifyOwnership(UUID vehicleId, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        Vehicle vehicle = findById(vehicleId);
        
        if (!vehicle.getOwner().getId().equals(user.getId())) {
            throw new ValidationException("Access denied - not owner");
        }
    }

    public VehicleDto setVehicleArchiveStatus(UUID vehicleId, boolean archived) {
        Vehicle vehicle = findById(vehicleId);
        vehicle.setIsArchived(archived);
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toDto(saved);
    }
}