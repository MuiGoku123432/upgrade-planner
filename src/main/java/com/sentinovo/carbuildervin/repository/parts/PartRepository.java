package com.sentinovo.carbuildervin.repository.parts;

import com.sentinovo.carbuildervin.entities.parts.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartRepository extends JpaRepository<Part, UUID> {

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId")
    List<Part> findByVehicleUpgradeId(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId")
    Page<Part> findByVehicleUpgradeId(@Param("upgradeId") UUID upgradeId, Pageable pageable);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId")
    List<Part> findByVehicleOwnerId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId")
    Page<Part> findByVehicleOwnerId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Part p WHERE p.id = :partId AND p.vehicleUpgrade.vehicle.owner.id = :userId")
    Optional<Part> findByIdAndOwnerId(@Param("partId") UUID partId, @Param("userId") UUID userId);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId AND p.vehicleUpgrade.vehicle.owner.id = :userId")
    List<Part> findByUpgradeIdAndOwnerId(@Param("upgradeId") UUID upgradeId, @Param("userId") UUID userId);

    @Query("SELECT p FROM Part p WHERE p.partCategory.code = :categoryCode")
    List<Part> findByPartCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT p FROM Part p WHERE p.partTier.code = :tierCode")
    List<Part> findByPartTierCode(@Param("tierCode") String tierCode);

    @Query("SELECT p FROM Part p WHERE p.status = :status")
    List<Part> findByStatus(@Param("status") String status);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND p.status = :status")
    List<Part> findByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId AND p.status = :status")
    List<Part> findByUpgradeIdAndStatus(@Param("upgradeId") UUID upgradeId, @Param("status") String status);

    @Query("SELECT p FROM Part p WHERE p.priorityValue = :priority")
    List<Part> findByPriorityValue(@Param("priority") Integer priority);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND p.priorityValue >= :minPriority")
    List<Part> findByOwnerIdAndMinPriority(@Param("userId") UUID userId, @Param("minPriority") Integer minPriority);

    @Query("SELECT p FROM Part p WHERE p.priorityValue >= :minPriority ORDER BY p.priorityValue DESC, p.createdAt")
    List<Part> findHighPriorityParts(@Param("minPriority") Integer minPriority);

    @Query("SELECT p FROM Part p WHERE p.isRequired = true")
    List<Part> findRequiredParts();

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId AND p.isRequired = true")
    List<Part> findRequiredPartsByUpgradeId(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT p FROM Part p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Part> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Part> findByOwnerIdAndPriceBetween(@Param("userId") UUID userId, 
                                           @Param("minPrice") BigDecimal minPrice, 
                                           @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Part p WHERE p.targetPurchaseDate BETWEEN :startDate AND :endDate")
    List<Part> findByTargetPurchaseDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND p.targetPurchaseDate BETWEEN :startDate AND :endDate")
    List<Part> findByOwnerIdAndTargetPurchaseDateBetween(@Param("userId") UUID userId, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Part p WHERE p.targetPurchaseDate <= :date AND p.status IN ('PLANNED', 'RESEARCHING')")
    List<Part> findOverdueParts(@Param("date") LocalDate date);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Part> searchByOwnerIdAndTerm(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND " +
           "(:categoryCode IS NULL OR p.partCategory.code = :categoryCode) AND " +
           "(:tierCode IS NULL OR p.partTier.code = :tierCode) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:minPriority IS NULL OR p.priorityValue >= :minPriority) AND " +
           "(:requiredOnly = false OR p.isRequired = true)")
    Page<Part> findByOwnerWithFilters(@Param("userId") UUID userId,
                                     @Param("categoryCode") String categoryCode,
                                     @Param("tierCode") String tierCode,
                                     @Param("status") String status,
                                     @Param("minPriority") Integer minPriority,
                                     @Param("requiredOnly") Boolean requiredOnly,
                                     Pageable pageable);

    @Query("SELECT SUM(p.price) FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId AND p.price IS NOT NULL")
    BigDecimal calculateTotalCostByUpgradeId(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT SUM(p.price) FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND p.price IS NOT NULL")
    BigDecimal calculateTotalCostByOwnerId(@Param("userId") UUID userId);

    @Query("SELECT SUM(p.price) FROM Part p WHERE p.vehicleUpgrade.vehicle.id = :vehicleId AND p.price IS NOT NULL")
    BigDecimal calculateTotalCostByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT COUNT(p) FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId")
    long countByUpgradeId(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT COUNT(p) FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId AND p.status = :status")
    long countByUpgradeIdAndStatus(@Param("upgradeId") UUID upgradeId, @Param("status") String status);

    @Query("SELECT COUNT(p) FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId AND p.status = :status")
    long countByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT p FROM Part p LEFT JOIN FETCH p.subParts WHERE p.id = :partId")
    Optional<Part> findByIdWithSubParts(@Param("partId") UUID partId);

    @Query("SELECT p FROM Part p LEFT JOIN FETCH p.subParts WHERE p.vehicleUpgrade.id = :upgradeId")
    List<Part> findByUpgradeIdWithSubParts(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.id = :upgradeId ORDER BY p.sortOrder, p.name")
    List<Part> findByUpgradeIdOrderBySortOrder(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT p FROM Part p WHERE p.vehicleUpgrade.vehicle.owner.id = :userId ORDER BY p.priorityValue DESC, p.createdAt")
    List<Part> findByOwnerIdOrderByPriority(@Param("userId") UUID userId);

    @Query("SELECT AVG(p.price) FROM Part p WHERE p.partCategory.code = :categoryCode AND p.price IS NOT NULL")
    BigDecimal calculateAveragePriceByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT AVG(p.price) FROM Part p WHERE p.partTier.code = :tierCode AND p.price IS NOT NULL")
    BigDecimal calculateAveragePriceByTierCode(@Param("tierCode") String tierCode);
}