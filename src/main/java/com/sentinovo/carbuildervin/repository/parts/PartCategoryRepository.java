package com.sentinovo.carbuildervin.repository.parts;

import com.sentinovo.carbuildervin.entities.parts.PartCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartCategoryRepository extends JpaRepository<PartCategory, String> {

    Optional<PartCategory> findByCode(String code);

    @Query("SELECT pc FROM PartCategory pc ORDER BY pc.sortOrder, pc.label")
    List<PartCategory> findAllOrderBySortOrderAndLabel();

    @Query("SELECT pc FROM PartCategory pc WHERE " +
           "LOWER(pc.label) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PartCategory> searchByCodeLabelOrDescription(@Param("searchTerm") String searchTerm);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM PartCategory pc " +
           "WHERE pc.code = :code AND pc.code != :excludeCode")
    boolean existsByCodeAndCodeNot(@Param("code") String code, @Param("excludeCode") String excludeCode);

    @Query("SELECT COUNT(p) FROM Part p WHERE p.partCategory.code = :categoryCode")
    long countPartsByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT COUNT(sp) FROM SubPart sp WHERE sp.partCategory.code = :categoryCode")
    long countSubPartsByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT pc FROM PartCategory pc WHERE pc.sortOrder = :sortOrder")
    List<PartCategory> findBySortOrder(@Param("sortOrder") Integer sortOrder);

    @Query("SELECT MAX(pc.sortOrder) FROM PartCategory pc")
    Integer findMaxSortOrder();

    @Query("SELECT MIN(pc.sortOrder) FROM PartCategory pc")
    Integer findMinSortOrder();
}