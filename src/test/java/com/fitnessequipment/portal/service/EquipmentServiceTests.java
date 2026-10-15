package com.fitnessequipment.portal.service;

import com.fitnessequipment.portal.dto.EquipmentRequestDto;
import com.fitnessequipment.portal.model.Equipment;
import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EquipmentServiceTests {

    @Autowired
    private EquipmentService equipmentService;

    @Test
    @DisplayName("Should successfully create a new equipment entry")
    void testCreateEquipment() {
        EquipmentRequestDto dto = new EquipmentRequestDto();
        dto.setName("Rowing Ergometer PM5");
        dto.setType(EquipmentType.ROWING_MACHINE);
        dto.setSerialNumber("TEST-ROW-999");
        dto.setGymZone("Cardio Annex");
        dto.setManufacturer("Concept2");
        dto.setModelNumber("Model D");
        dto.setInstallationDate(LocalDate.now());
        dto.setCurrentStatus(MaintenanceStatus.OPERATIONAL);

        Equipment saved = equipmentService.createEquipment(dto);

        assertNotNull(saved.getId(), "Generated ID must not be null");
        assertEquals("Rowing Ergometer PM5", saved.getName());
        assertEquals(EquipmentType.ROWING_MACHINE, saved.getType());
        assertEquals("TEST-ROW-999", saved.getSerialNumber());
    }

    @Test
    @DisplayName("Should retrieve equipment by unique serial number")
    void testGetBySerialNumber() {
        EquipmentRequestDto dto = new EquipmentRequestDto();
        dto.setName("Olympic Incline Bench");
        dto.setType(EquipmentType.BENCH_PRESS);
        dto.setSerialNumber("TEST-BENCH-007");
        dto.setGymZone("Free Weights Area");
        dto.setCurrentStatus(MaintenanceStatus.OPERATIONAL);

        equipmentService.createEquipment(dto);

        Optional<Equipment> found = equipmentService.getEquipmentBySerialNumber("TEST-BENCH-007");
        assertTrue(found.isPresent(), "Equipment should be found by serial number");
        assertEquals("Free Weights Area", found.get().getGymZone());
    }

    @Test
    @DisplayName("Should filter and search equipment by query text and zone")
    void testSearchEquipment() {
        List<Equipment> results = equipmentService.searchEquipment("Treadmill", null, null, null);
        assertNotNull(results);
        assertTrue(results.size() > 0, "Should find at least one treadmill from sample data");
    }

    @Test
    @DisplayName("Should update equipment status")
    void testUpdateStatus() {
        EquipmentRequestDto dto = new EquipmentRequestDto();
        dto.setName("Cable Tower Unit 3");
        dto.setType(EquipmentType.CABLE_CROSSOVER);
        dto.setSerialNumber("TEST-CABLE-333");
        dto.setGymZone("Strength Floor");
        dto.setCurrentStatus(MaintenanceStatus.OPERATIONAL);

        Equipment created = equipmentService.createEquipment(dto);
        equipmentService.updateStatus(created.getId(), MaintenanceStatus.UNDER_INSPECTION);

        Equipment updated = equipmentService.getEquipmentById(created.getId()).orElseThrow();
        assertEquals(MaintenanceStatus.UNDER_INSPECTION, updated.getCurrentStatus());
    }
}
