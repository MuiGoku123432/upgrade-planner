package com.sentinovo.carbuildervin.repository.parts;

import com.sentinovo.carbuildervin.entities.parts.SubPart;
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
public interface SubPartRepository extends JpaRepository<SubPart, UUID> {

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.id = :partId")
    List<SubPart> findByParentPartId(@Param("partId") UUID partId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.id = :partId")
    Page<SubPart> findByParentPartId(@Param("partId") UUID partId, Pageable pageable);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId")
    List<SubPart> findByVehicleOwnerId(@Param("userId") UUID userId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId")
    Page<SubPart> findByVehicleOwnerId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT sp FROM SubPart sp WHERE sp.id = :subPartId AND sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId")
    Optional<SubPart> findByIdAndOwnerId(@Param("subPartId") UUID subPartId, @Param("userId") UUID userId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.id = :partId AND sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId")
    List<SubPart> findByParentPartIdAndOwnerId(@Param("partId") UUID partId, @Param("userId") UUID userId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.id = :upgradeId")
    List<SubPart> findByUpgradeId(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.id = :vehicleId")
    List<SubPart> findByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.partCategory.code = :categoryCode")
    List<SubPart> findByPartCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT sp FROM SubPart sp WHERE sp.partTier.code = :tierCode")
    List<SubPart> findByPartTierCode(@Param("tierCode") String tierCode);

    @Query("SELECT sp FROM SubPart sp WHERE sp.status = :status")
    List<SubPart> findByStatus(@Param("status") String status);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND sp.status = :status")
    List<SubPart> findByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.id = :partId AND sp.status = :status")
    List<SubPart> findByParentPartIdAndStatus(@Param("partId") UUID partId, @Param("status") String status);

    @Query("SELECT sp FROM SubPart sp WHERE sp.priorityValue = :priority")
    List<SubPart> findByPriorityValue(@Param("priority") Integer priority);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND sp.priorityValue >= :minPriority")
    List<SubPart> findByOwnerIdAndMinPriority(@Param("userId") UUID userId, @Param("minPriority") Integer minPriority);

    @Query("SELECT sp FROM SubPart sp WHERE sp.priorityValue >= :minPriority ORDER BY sp.priorityValue DESC, sp.createdAt")
    List<SubPart> findHighPrioritySubParts(@Param("minPriority") Integer minPriority);

    @Query("SELECT sp FROM SubPart sp WHERE sp.isRequired = true")
    List<SubPart> findRequiredSubParts();

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.id = :partId AND sp.isRequired = true")
    List<SubPart> findRequiredSubPartsByParentPartId(@Param("partId") UUID partId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.price BETWEEN :minPrice AND :maxPrice")
    List<SubPart> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND sp.price BETWEEN :minPrice AND :maxPrice")
    List<SubPart> findByOwnerIdAndPriceBetween(@Param("userId") UUID userId, 
                                              @Param("minPrice") BigDecimal minPrice, 
                                              @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT sp FROM SubPart sp WHERE sp.targetPurchaseDate BETWEEN :startDate AND :endDate")
    List<SubPart> findByTargetPurchaseDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND sp.targetPurchaseDate BETWEEN :startDate AND :endDate")
    List<SubPart> findByOwnerIdAndTargetPurchaseDateBetween(@Param("userId") UUID userId, 
                                                            @Param("startDate") LocalDate startDate, 
                                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT sp FROM SubPart sp WHERE sp.targetPurchaseDate <= :date AND sp.status IN ('PLANNED', 'RESEARCHING')")
    List<SubPart> findOverdueSubParts(@Param("date") LocalDate date);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND " +
           "(LOWER(sp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sp.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sp.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<SubPart> searchByOwnerIdAndTerm(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND " +
           "(:categoryCode IS NULL OR sp.partCategory.code = :categoryCode) AND " +
           "(:tierCode IS NULL OR sp.partTier.code = :tierCode) AND " +
           "(:status IS NULL OR sp.status = :status) AND " +
           "(:minPriority IS NULL OR sp.priorityValue >= :minPriority) AND " +
           "(:requiredOnly = false OR sp.isRequired = true)")
    Page<SubPart> findByOwnerWithFilters(@Param("userId") UUID userId,
                                        @Param("categoryCode") String categoryCode,
                                        @Param("tierCode") String tierCode,
                                        @Param("status") String status,
                                        @Param("minPriority") Integer minPriority,
                                        @Param("requiredOnly") Boolean requiredOnly,
                                        Pageable pageable);

    @Query("SELECT SUM(sp.price) FROM SubPart sp WHERE sp.parentPart.id = :partId AND sp.price IS NOT NULL")
    BigDecimal calculateTotalCostByParentPartId(@Param("partId") UUID partId);

    @Query("SELECT SUM(sp.price) FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.id = :upgradeId AND sp.price IS NOT NULL")
    BigDecimal calculateTotalCostByUpgradeId(@Param("upgradeId") UUID upgradeId);

    @Query("SELECT SUM(sp.price) FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND sp.price IS NOT NULL")
    BigDecimal calculateTotalCostByOwnerId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(sp) FROM SubPart sp WHERE sp.parentPart.id = :partId")
    long countByParentPartId(@Param("partId") UUID partId);

    @Query("SELECT COUNT(sp) FROM SubPart sp WHERE sp.parentPart.id = :partId AND sp.status = :status")
    long countByParentPartIdAndStatus(@Param("partId") UUID partId, @Param("status") String status);

    @Query("SELECT COUNT(sp) FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND sp.status = :status")
    long countByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.id = :partId ORDER BY sp.sortOrder, sp.name")
    List<SubPart> findByParentPartIdOrderBySortOrder(@Param("partId") UUID partId);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId ORDER BY sp.priorityValue DESC, sp.createdAt")
    List<SubPart> findByOwnerIdOrderByPriority(@Param("userId") UUID userId);

    @Query("SELECT AVG(sp.price) FROM SubPart sp WHERE sp.partCategory.code = :categoryCode AND sp.price IS NOT NULL")
    BigDecimal calculateAveragePriceByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT AVG(sp.price) FROM SubPart sp WHERE sp.partTier.code = :tierCode AND sp.price IS NOT NULL")
    BigDecimal calculateAveragePriceByTierCode(@Param("tierCode") String tierCode);

    @Query("SELECT sp FROM SubPart sp WHERE sp.parentPart.vehicleUpgrade.vehicle.owner.id = :userId AND " +
           "sp.parentPart.id = :partId AND " +
           "(LOWER(sp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sp.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<SubPart> searchByParentPartAndTerm(@Param("userId") UUID userId, 
                                           @Param("partId") UUID partId, 
                                           @Param("searchTerm") String searchTerm);
}