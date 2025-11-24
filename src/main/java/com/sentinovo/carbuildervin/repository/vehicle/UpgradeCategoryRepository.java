package com.sentinovo.carbuildervin.repository.vehicle;

import com.sentinovo.carbuildervin.entities.vehicle.UpgradeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpgradeCategoryRepository extends JpaRepository<UpgradeCategory, Integer> {

    Optional<UpgradeCategory> findByName(String name);

    @Query("SELECT uc FROM UpgradeCategory uc ORDER BY uc.sortOrder, uc.name")
    List<UpgradeCategory> findAllOrderBySortOrderAndName();

    @Query("SELECT uc FROM UpgradeCategory uc WHERE uc.isActive = true ORDER BY uc.sortOrder, uc.name")
    List<UpgradeCategory> findActiveOrderBySortOrderAndName();

    @Query("SELECT uc FROM UpgradeCategory uc WHERE " +
           "LOWER(uc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(uc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UpgradeCategory> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END FROM UpgradeCategory uc " +
           "WHERE uc.name = :name AND uc.id != :categoryId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("categoryId") Integer categoryId);

    @Query("SELECT COUNT(vu) FROM VehicleUpgrade vu WHERE vu.upgradeCategory.id = :categoryId")
    long countVehicleUpgradesByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT uc FROM UpgradeCategory uc WHERE uc.isActive = :isActive")
    List<UpgradeCategory> findByIsActive(@Param("isActive") Boolean isActive);

    @Query("SELECT MAX(uc.sortOrder) FROM UpgradeCategory uc")
    Integer findMaxSortOrder();
}