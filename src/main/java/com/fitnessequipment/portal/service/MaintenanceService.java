package com.fitnessequipment.portal.service;

import com.fitnessequipment.portal.dto.MaintenanceRequestDto;
import com.fitnessequipment.portal.model.Equipment;
import com.fitnessequipment.portal.model.MaintenanceLog;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import com.fitnessequipment.portal.model.PriorityLevel;
import com.fitnessequipment.portal.repository.EquipmentRepository;
import com.fitnessequipment.portal.repository.MaintenanceLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final EquipmentRepository equipmentRepository;

    public MaintenanceService(MaintenanceLogRepository maintenanceLogRepository,
                              EquipmentRepository equipmentRepository) {
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public List<MaintenanceLog> getAllLogs() {
        return maintenanceLogRepository.findAll();
    }

    public List<MaintenanceLog> getActiveBreakdowns() {
        return maintenanceLogRepository.findActiveBreakdowns();
    }

    public Optional<MaintenanceLog> getLogById(Long id) {
        return maintenanceLogRepository.findById(id);
    }

    public List<MaintenanceLog> getLogsForEquipment(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + equipmentId));
        return maintenanceLogRepository.findByEquipmentOrderByReportedAtDesc(equipment);
    }

    public List<MaintenanceLog> filterLogs(MaintenanceStatus status, PriorityLevel priority) {
        return maintenanceLogRepository.filterLogs(status, priority);
    }

    public MaintenanceLog createLog(MaintenanceRequestDto dto) {
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + dto.getEquipmentId()));

        MaintenanceStatus status = dto.getStatus() != null ? dto.getStatus() : MaintenanceStatus.REPORTED;
        MaintenanceLog log = new MaintenanceLog(
                equipment,
                dto.getReportedBy(),
                dto.getReporterRole() != null ? dto.getReporterRole() : "TRAINER",
                dto.getIssueTitle(),
                dto.getIssueDescription(),
                dto.getPriority() != null ? dto.getPriority() : PriorityLevel.MEDIUM,
                status
        );
        log.setAssignedTechnician(dto.getAssignedTechnician());
        log.setEstimatedCost(dto.getEstimatedCost() != null ? dto.getEstimatedCost() : 0.0);
        log.setActualCost(dto.getActualCost() != null ? dto.getActualCost() : 0.0);
        log.setResolutionNotes(dto.getResolutionNotes());

        // Synchronize equipment status
        equipment.setCurrentStatus(status);
        equipmentRepository.save(equipment);

        return maintenanceLogRepository.save(log);
    }

    public MaintenanceLog updateWorkflowStatus(Long logId, MaintenanceStatus newStatus,
                                               String actorRole, String technician,
                                               String notes, Double actualCost) {
        MaintenanceLog log = maintenanceLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance Log not found with ID: " + logId));

        validateStatusTransition(log.getStatus(), newStatus, actorRole);

        log.setStatus(newStatus);
        if (technician != null && !technician.trim().isEmpty()) {
            log.setAssignedTechnician(technician);
        }
        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = log.getResolutionNotes() != null ? log.getResolutionNotes() + "\n" : "";
            log.setResolutionNotes(existingNotes + "[" + LocalDateTime.now() + " by " + actorRole + "]: " + notes);
        }
        if (actualCost != null && actualCost > 0) {
            log.setActualCost(actualCost);
        }
        if (newStatus == MaintenanceStatus.OPERATIONAL) {
            log.setResolvedAt(LocalDateTime.now());
            // Update equipment inspection date
            Equipment eq = log.getEquipment();
            eq.setLastInspectionDate(LocalDate.now());
        }

        // Synchronize parent equipment status
        Equipment equipment = log.getEquipment();
        equipment.setCurrentStatus(newStatus);
        equipmentRepository.save(equipment);

        return maintenanceLogRepository.save(log);
    }

    private void validateStatusTransition(MaintenanceStatus current, MaintenanceStatus next, String role) {
        // Validation rules:
        // Role permissions:
        // - TRAINER can report
        // - TECHNICIAN can move to UNDER_INSPECTION, PARTS_ORDERED, IN_REPAIR, OPERATIONAL
        // - FACILITY_ADMIN can transition to any status including DECOMMISSIONED
        if (role == null) {
            role = "FACILITY_ADMIN";
        }
        String cleanRole = role.toUpperCase();
        if (cleanRole.contains("TRAINER") && next != MaintenanceStatus.REPORTED) {
            throw new IllegalStateException("Trainers can only submit initial reports. Technician or Admin review required.");
        }
        if (next == MaintenanceStatus.DECOMMISSIONED && !cleanRole.contains("ADMIN")) {
            throw new IllegalStateException("Only Facility Admins can mark equipment as DECOMMISSIONED.");
        }
    }

    public void deleteLog(Long id) {
        maintenanceLogRepository.deleteById(id);
    }
}
