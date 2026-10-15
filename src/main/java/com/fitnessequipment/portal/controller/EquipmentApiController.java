package com.fitnessequipment.portal.controller;

import com.fitnessequipment.portal.dto.EquipmentRequestDto;
import com.fitnessequipment.portal.model.Equipment;
import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import com.fitnessequipment.portal.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentApiController {

    private final EquipmentService equipmentService;

    public EquipmentApiController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EquipmentType type,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) String zone) {

        if (query != null || type != null || status != null || zone != null) {
            return ResponseEntity.ok(equipmentService.searchEquipment(query, type, status, zone));
        }
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-serial/{serial}")
    public ResponseEntity<Equipment> getEquipmentBySerial(@PathVariable String serial) {
        return equipmentService.getEquipmentBySerialNumber(serial)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zones")
    public ResponseEntity<List<String>> getAllZones() {
        return ResponseEntity.ok(equipmentService.getAllGymZones());
    }

    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@Valid @RequestBody EquipmentRequestDto dto) {
        Equipment created = equipmentService.createEquipment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id,
                                                     @Valid @RequestBody EquipmentRequestDto dto) {
        try {
            Equipment updated = equipmentService.updateEquipment(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestParam MaintenanceStatus status) {
        try {
            equipmentService.updateStatus(id, status);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
