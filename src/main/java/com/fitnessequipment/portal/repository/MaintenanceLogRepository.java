package com.fitnessequipment.portal.repository;

import com.fitnessequipment.portal.model.Equipment;
import com.fitnessequipment.portal.model.MaintenanceLog;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import com.fitnessequipment.portal.model.PriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findByEquipmentOrderByReportedAtDesc(Equipment equipment);

    List<MaintenanceLog> findByStatusOrderByReportedAtDesc(MaintenanceStatus status);

    List<MaintenanceLog> findByPriorityOrderByReportedAtDesc(PriorityLevel priority);

    long countByStatus(MaintenanceStatus status);

    long countByPriority(PriorityLevel priority);

    @Query("SELECT m FROM MaintenanceLog m WHERE " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:priority IS NULL OR m.priority = :priority) " +
           "ORDER BY m.reportedAt DESC")
    List<MaintenanceLog> filterLogs(
            @Param("status") MaintenanceStatus status,
            @Param("priority") PriorityLevel priority);

    @Query("SELECT m FROM MaintenanceLog m WHERE m.status != 'OPERATIONAL' AND m.status != 'DECOMMISSIONED' ORDER BY m.reportedAt DESC")
    List<MaintenanceLog> findActiveBreakdowns();
}
