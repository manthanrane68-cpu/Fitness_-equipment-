package com.fitnessequipment.portal.service;

import com.fitnessequipment.portal.dto.EquipmentRequestDto;
import com.fitnessequipment.portal.dto.MaintenanceRequestDto;
import com.fitnessequipment.portal.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MaintenanceServiceTests {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private EquipmentService equipmentService;

    @Test
    @DisplayName("Should create maintenance ticket and update equipment status to REPORTED")
    void testCreateMaintenanceTicket() {
        EquipmentRequestDto eqDto = new EquipmentRequestDto();
        eqDto.setName("Stair Climber Pro");
        eqDto.setType(EquipmentType.TREADMILL);
        eqDto.setSerialNumber("TEST-STAIR-101");
        eqDto.setGymZone("Cardio Zone B");
        eqDto.setCurrentStatus(MaintenanceStatus.OPERATIONAL);
        Equipment eq = equipmentService.createEquipment(eqDto);

        MaintenanceRequestDto req = new MaintenanceRequestDto();
        req.setEquipmentId(eq.getId());
        req.setReportedBy("Trainer Mike");
        req.setReporterRole("TRAINER");
        req.setIssueTitle("Step chain slippage");
        req.setIssueDescription("Chain makes slipping gear sound on resistance level 8+");
        req.setPriority(PriorityLevel.HIGH);
        req.setStatus(MaintenanceStatus.REPORTED);

        MaintenanceLog log = maintenanceService.createLog(req);

        assertNotNull(log.getId());
        assertEquals(MaintenanceStatus.REPORTED, log.getStatus());
        assertEquals("Trainer Mike", log.getReportedBy());

        // Check equipment status synced
        Equipment refreshed = equipmentService.getEquipmentById(eq.getId()).orElseThrow();
        assertEquals(MaintenanceStatus.REPORTED, refreshed.getCurrentStatus());
    }

    @Test
    @DisplayName("Should advance workflow through technician stages and resolve to OPERATIONAL")
    void testTechnicianWorkflowTransitions() {
        EquipmentRequestDto eqDto = new EquipmentRequestDto();
        eqDto.setName("Air Bike Elite");
        eqDto.setType(EquipmentType.STATIONARY_BIKE);
        eqDto.setSerialNumber("TEST-BIKE-555");
        eqDto.setGymZone("Studio 2");
        Equipment eq = equipmentService.createEquipment(eqDto);

        MaintenanceRequestDto req = new MaintenanceRequestDto();
        req.setEquipmentId(eq.getId());
        req.setReportedBy("Trainer Sam");
        req.setIssueTitle("Pedal crank loose");
        req.setPriority(PriorityLevel.MEDIUM);
        MaintenanceLog log = maintenanceService.createLog(req);

        // Transition 1: Technician moves to UNDER_INSPECTION
        MaintenanceLog inspected = maintenanceService.updateWorkflowStatus(
                log.getId(), MaintenanceStatus.UNDER_INSPECTION, "TECHNICIAN", "Dave Tech", "Assessing bottom bracket", null);
        assertEquals(MaintenanceStatus.UNDER_INSPECTION, inspected.getStatus());
        assertEquals("Dave Tech", inspected.getAssignedTechnician());

        // Transition 2: Technician moves to IN_REPAIR
        MaintenanceLog repairing = maintenanceService.updateWorkflowStatus(
                log.getId(), MaintenanceStatus.IN_REPAIR, "TECHNICIAN", "Dave Tech", "Tightening crank arm to 45Nm", null);
        assertEquals(MaintenanceStatus.IN_REPAIR, repairing.getStatus());

        // Transition 3: Resolved to OPERATIONAL
        MaintenanceLog resolved = maintenanceService.updateWorkflowStatus(
                log.getId(), MaintenanceStatus.OPERATIONAL, "TECHNICIAN", "Dave Tech", "Torqued and road-tested", 45.0);
        assertEquals(MaintenanceStatus.OPERATIONAL, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());
        assertEquals(45.0, resolved.getActualCost());

        // Equipment status must also be OPERATIONAL
        Equipment refreshed = equipmentService.getEquipmentById(eq.getId()).orElseThrow();
        assertEquals(MaintenanceStatus.OPERATIONAL, refreshed.getCurrentStatus());
    }

    @Test
    @DisplayName("Should prevent unauthorized roles from moving tickets to DECOMMISSIONED")
    void testDecommissionRestriction() {
        EquipmentRequestDto eqDto = new EquipmentRequestDto();
        eqDto.setName("Old Elliptical");
        eqDto.setType(EquipmentType.ELLIPTICAL);
        eqDto.setSerialNumber("TEST-OLD-900");
        eqDto.setGymZone("Cardio Zone A");
        Equipment eq = equipmentService.createEquipment(eqDto);

        MaintenanceRequestDto req = new MaintenanceRequestDto();
        req.setEquipmentId(eq.getId());
        req.setReportedBy("Trainer Sam");
        req.setIssueTitle("Broken frame");
        req.setPriority(PriorityLevel.CRITICAL_SAFETY_HAZARD);
        MaintenanceLog log = maintenanceService.createLog(req);

        // Trainer cannot decommission
        assertThrows(IllegalStateException.class, () -> {
            maintenanceService.updateWorkflowStatus(
                    log.getId(), MaintenanceStatus.DECOMMISSIONED, "TRAINER", "Sam", "Throw away", null);
        });

        // Facility Admin CAN decommission
        MaintenanceLog decommissioned = maintenanceService.updateWorkflowStatus(
                log.getId(), MaintenanceStatus.DECOMMISSIONED, "FACILITY_ADMIN", "Admin Boss", "Asset retired", null);
        assertEquals(MaintenanceStatus.DECOMMISSIONED, decommissioned.getStatus());
    }
}
