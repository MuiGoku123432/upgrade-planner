package com.sentinovo.carbuildervin.repository.vehicle;

import com.sentinovo.carbuildervin.entities.vehicle.VehicleUpgrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleUpgradeRepository extends JpaRepository<VehicleUpgrade, UUID> {

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId")
    List<VehicleUpgrade> findByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId")
    Page<VehicleUpgrade> findByVehicleId(@Param("vehicleId") UUID vehicleId, Pageable pageable);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId")
    List<VehicleUpgrade> findByVehicleOwnerId(@Param("userId") UUID userId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId")
    Page<VehicleUpgrade> findByVehicleOwnerId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId AND vu.vehicle.owner.id = :userId")
    List<VehicleUpgrade> findByVehicleIdAndOwnerId(@Param("vehicleId") UUID vehicleId, @Param("userId") UUID userId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.id = :upgradeId AND vu.vehicle.owner.id = :userId")
    Optional<VehicleUpgrade> findByIdAndOwnerId(@Param("upgradeId") UUID upgradeId, @Param("userId") UUID userId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.upgradeCategory.id = :categoryId")
    List<VehicleUpgrade> findByUpgradeCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.status = :status")
    List<VehicleUpgrade> findByStatus(@Param("status") String status);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId AND vu.status = :status")
    List<VehicleUpgrade> findByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId AND vu.status = :status")
    List<VehicleUpgrade> findByVehicleIdAndStatus(@Param("vehicleId") UUID vehicleId, @Param("status") String status);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.priorityLevel = :priorityLevel")
    List<VehicleUpgrade> findByPriorityLevel(@Param("priorityLevel") Integer priorityLevel);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId AND vu.priorityLevel >= :minPriority")
    List<VehicleUpgrade> findByOwnerIdAndMinPriority(@Param("userId") UUID userId, @Param("minPriority") Integer minPriority);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.isPrimaryForCategory = true")
    List<VehicleUpgrade> findPrimaryUpgrades();

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId AND vu.isPrimaryForCategory = true")
    List<VehicleUpgrade> findPrimaryUpgradesByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId AND vu.upgradeCategory.id = :categoryId AND vu.isPrimaryForCategory = true")
    Optional<VehicleUpgrade> findPrimaryUpgradeByVehicleAndCategory(@Param("vehicleId") UUID vehicleId, @Param("categoryId") Integer categoryId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.targetCompletionDate BETWEEN :startDate AND :endDate")
    List<VehicleUpgrade> findByTargetCompletionDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId AND vu.targetCompletionDate BETWEEN :startDate AND :endDate")
    List<VehicleUpgrade> findByOwnerIdAndTargetCompletionDateBetween(@Param("userId") UUID userId, 
                                                                     @Param("startDate") LocalDate startDate, 
                                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.targetCompletionDate <= :date AND vu.status IN ('PLANNED', 'IN_PROGRESS')")
    List<VehicleUpgrade> findOverdueUpgrades(@Param("date") LocalDate date);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId AND " +
           "(LOWER(vu.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(vu.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(vu.upgradeCategory.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<VehicleUpgrade> searchByOwnerIdAndTerm(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(vu) FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId")
    long countByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT COUNT(vu) FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId AND vu.status = :status")
    long countByVehicleIdAndStatus(@Param("vehicleId") UUID vehicleId, @Param("status") String status);

    @Query("SELECT COUNT(vu) FROM VehicleUpgrade vu WHERE vu.vehicle.owner.id = :userId AND vu.status = :status")
    long countByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT vu FROM VehicleUpgrade vu LEFT JOIN FETCH vu.parts WHERE vu.id = :upgradeId")
    Optional<VehicleUpgrade> findByIdWithParts(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT vu FROM VehicleUpgrade vu LEFT JOIN FETCH vu.parts WHERE vu.vehicle.id = :vehicleId")
    List<VehicleUpgrade> findByVehicleIdWithParts(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.slug = :slug")
    Optional<VehicleUpgrade> findBySlug(@Param("slug") String slug);

    @Query("SELECT vu FROM VehicleUpgrade vu WHERE vu.vehicle.id = :vehicleId AND vu.slug = :slug")
    Optional<VehicleUpgrade> findByVehicleIdAndSlug(@Param("vehicleId") UUID vehicleId, @Param("slug") String slug);
}