package com.fitnessequipment.portal.repository;

import com.fitnessequipment.portal.model.Equipment;
import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findBySerialNumber(String serialNumber);

    List<Equipment> findByCurrentStatus(MaintenanceStatus currentStatus);

    List<Equipment> findByType(EquipmentType type);

    List<Equipment> findByGymZoneIgnoreCase(String gymZone);

    @Query("SELECT e FROM Equipment e WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.gymZone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.manufacturer) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:status IS NULL OR e.currentStatus = :status) AND " +
           "(:zone IS NULL OR :zone = '' OR LOWER(e.gymZone) = LOWER(:zone))")
    List<Equipment> searchEquipment(
            @Param("query") String query,
            @Param("type") EquipmentType type,
            @Param("status") MaintenanceStatus status,
            @Param("zone") String zone);

    long countByCurrentStatus(MaintenanceStatus status);

    @Query("SELECT DISTINCT e.gymZone FROM Equipment e")
    List<String> findDistinctGymZones();
}
