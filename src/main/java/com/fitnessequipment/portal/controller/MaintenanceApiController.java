package com.fitnessequipment.portal.controller;

import com.fitnessequipment.portal.dto.MaintenanceRequestDto;
import com.fitnessequipment.portal.model.MaintenanceLog;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import com.fitnessequipment.portal.model.PriorityLevel;
import com.fitnessequipment.portal.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceApiController {

    private final MaintenanceService maintenanceService;

    public MaintenanceApiController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceLog>> getAllLogs(
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) PriorityLevel priority) {
        if (status != null || priority != null) {
            return ResponseEntity.ok(maintenanceService.filterLogs(status, priority));
        }
        return ResponseEntity.ok(maintenanceService.getAllLogs());
    }

    @GetMapping("/active")
    public ResponseEntity<List<MaintenanceLog>> getActiveBreakdowns() {
        return ResponseEntity.ok(maintenanceService.getActiveBreakdowns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceLog> getLogById(@PathVariable Long id) {
        return maintenanceService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<MaintenanceLog>> getLogsForEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(maintenanceService.getLogsForEquipment(equipmentId));
    }

    @PostMapping
    public ResponseEntity<MaintenanceLog> createLog(@Valid @RequestBody MaintenanceRequestDto dto) {
        MaintenanceLog created = maintenanceService.createLog(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/workflow")
    public ResponseEntity<?> updateWorkflow(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            MaintenanceStatus newStatus = MaintenanceStatus.valueOf((String) payload.get("status"));
            String role = (String) payload.getOrDefault("role", "TECHNICIAN");
            String technician = (String) payload.get("technician");
            String notes = (String) payload.get("notes");
            Double actualCost = payload.get("actualCost") != null ?
                    Double.valueOf(payload.get("actualCost").toString()) : null;

            MaintenanceLog updated = maintenanceService.updateWorkflowStatus(
                    id, newStatus, role, technician, notes, actualCost);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        maintenanceService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}
