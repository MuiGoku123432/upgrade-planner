package com.sentinovo.carbuildervin.repository.vehicle;

import com.sentinovo.carbuildervin.entities.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND v.isArchived = false")
    List<Vehicle> findByOwnerIdAndNotDeleted(@Param("userId") UUID userId);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND v.isArchived = false")
    Page<Vehicle> findByOwnerIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.vin = :vin AND v.isArchived = false")
    Optional<Vehicle> findByVinAndNotDeleted(@Param("vin") String vin);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND v.id = :vehicleId AND v.isArchived = false")
    Optional<Vehicle> findByIdAndOwnerIdAndNotDeleted(@Param("vehicleId") UUID vehicleId, @Param("userId") UUID userId);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND " +
           "(LOWER(v.vin) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "CAST(v.year AS string) LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(v.make) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.nickname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "v.isArchived = false")
    Page<Vehicle> searchByOwnerAndNotDeleted(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.vehicleUpgrades WHERE v.owner.id = :userId AND v.isArchived = false")
    List<Vehicle> findByOwnerIdWithUpgrades(@Param("userId") UUID userId);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.owner.id = :userId AND v.isArchived = false")
    long countByOwnerIdAndNotDeleted(@Param("userId") UUID userId);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.vin = :vin AND v.isArchived = false")
    long countByVinAndNotDeleted(@Param("vin") String vin);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehicle v " +
           "WHERE v.vin = :vin AND v.id != :vehicleId AND v.isArchived = false")
    boolean existsByVinAndIdNotAndNotDeleted(@Param("vin") String vin, @Param("vehicleId") UUID vehicleId);

    @Query("SELECT v FROM Vehicle v WHERE v.isArchived = false AND v.createdAt >= :since")
    List<Vehicle> findRecentVehicles(@Param("since") LocalDateTime since);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND v.vin IS NULL AND v.isArchived = false")
    List<Vehicle> findProjectVehiclesByOwner(@Param("userId") UUID userId);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND v.vin IS NOT NULL AND v.isArchived = false")
    List<Vehicle> findRealVehiclesByOwner(@Param("userId") UUID userId);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId AND " +
           "(:vinFilter IS NULL OR " +
           "(:vinFilter = 'HAS_VIN' AND v.vin IS NOT NULL) OR " +
           "(:vinFilter = 'NO_VIN' AND v.vin IS NULL)) AND " +
           "v.isArchived = false")
    Page<Vehicle> findByOwnerWithVinFilter(@Param("userId") UUID userId, 
                                          @Param("vinFilter") String vinFilter, 
                                          Pageable pageable);

    @Query(value = "SELECT v.* FROM vehicle v WHERE v.owner_id = :userId AND " +
           "v.is_archived = false AND " +
           "(:year IS NULL OR v.year = :year) AND " +
           "(:make IS NULL OR LOWER(v.make) = LOWER(:make)) AND " +
           "(:model IS NULL OR LOWER(v.model) = LOWER(:model))",
           nativeQuery = true)
    List<Vehicle> findByOwnerAndSpecs(@Param("userId") UUID userId,
                                     @Param("year") String year,
                                     @Param("make") String make,
                                     @Param("model") String model);
}