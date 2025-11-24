package com.sentinovo.carbuildervin.repository.parts;

import com.sentinovo.carbuildervin.entities.parts.PartTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartTierRepository extends JpaRepository<PartTier, String> {

    Optional<PartTier> findByCode(String code);

    @Query("SELECT pt FROM PartTier pt ORDER BY pt.rank")
    List<PartTier> findAllOrderByRank();

    @Query("SELECT pt FROM PartTier pt WHERE pt.rank = :rank")
    List<PartTier> findByRank(@Param("rank") Integer rank);

    @Query("SELECT pt FROM PartTier pt WHERE pt.rank >= :minRank ORDER BY pt.rank")
    List<PartTier> findByRankGreaterThanEqual(@Param("minRank") Integer minRank);

    @Query("SELECT pt FROM PartTier pt WHERE pt.rank <= :maxRank ORDER BY pt.rank")
    List<PartTier> findByRankLessThanEqual(@Param("maxRank") Integer maxRank);

    @Query("SELECT pt FROM PartTier pt WHERE pt.rank BETWEEN :minRank AND :maxRank ORDER BY pt.rank")
    List<PartTier> findByRankBetween(@Param("minRank") Integer minRank, @Param("maxRank") Integer maxRank);

    @Query("SELECT pt FROM PartTier pt WHERE " +
           "LOWER(pt.label) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pt.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PartTier> searchByCodeLabelOrDescription(@Param("searchTerm") String searchTerm);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(pt) > 0 THEN true ELSE false END FROM PartTier pt " +
           "WHERE pt.code = :code AND pt.code != :excludeCode")
    boolean existsByCodeAndCodeNot(@Param("code") String code, @Param("excludeCode") String excludeCode);

    @Query("SELECT CASE WHEN COUNT(pt) > 0 THEN true ELSE false END FROM PartTier pt " +
           "WHERE pt.rank = :rank AND pt.code != :excludeCode")
    boolean existsByRankAndCodeNot(@Param("rank") Integer rank, @Param("excludeCode") String excludeCode);

    @Query("SELECT COUNT(p) FROM Part p WHERE p.partTier.code = :tierCode")
    long countPartsByTierCode(@Param("tierCode") String tierCode);

    @Query("SELECT COUNT(sp) FROM SubPart sp WHERE sp.partTier.code = :tierCode")
    long countSubPartsByTierCode(@Param("tierCode") String tierCode);

    @Query("SELECT MAX(pt.rank) FROM PartTier pt")
    Integer findMaxRank();

    @Query("SELECT MIN(pt.rank) FROM PartTier pt")
    Integer findMinRank();
}