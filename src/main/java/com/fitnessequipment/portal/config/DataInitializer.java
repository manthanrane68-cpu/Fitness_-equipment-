package com.fitnessequipment.portal.config;

import com.fitnessequipment.portal.model.*;
import com.fitnessequipment.portal.repository.EquipmentRepository;
import com.fitnessequipment.portal.repository.MaintenanceLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EquipmentRepository equipmentRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;

    public DataInitializer(EquipmentRepository equipmentRepository,
                           MaintenanceLogRepository maintenanceLogRepository) {
        this.equipmentRepository = equipmentRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
    }

    @Override
    public void run(String... args) {
        if (equipmentRepository.count() > 0) {
            return;
        }

        Equipment eq1 = new Equipment("ProRunner Treadmill X9", EquipmentType.TREADMILL, "TM-2024-001",
                "Cardio Zone A", "LifeFitness", "PRX9-PRO", LocalDate.of(2024, 1, 15), MaintenanceStatus.OPERATIONAL);
        eq1.setLastInspectionDate(LocalDate.now().minusDays(5));

        Equipment eq2 = new Equipment("Commercial Ascent Elliptical", EquipmentType.ELLIPTICAL, "EL-2024-002",
                "Cardio Zone A", "Matrix Fitness", "A50-XER", LocalDate.of(2024, 2, 10), MaintenanceStatus.UNDER_INSPECTION);
        eq2.setLastInspectionDate(LocalDate.now().minusDays(2));

        Equipment eq3 = new Equipment("Concept2 Indoor Rower", EquipmentType.ROWING_MACHINE, "RW-2023-003",
                "Functional Training Zone", "Concept2", "Model D PM5", LocalDate.of(2023, 6, 20), MaintenanceStatus.OPERATIONAL);
        eq3.setLastInspectionDate(LocalDate.now().minusDays(14));

        Equipment eq4 = new Equipment("Dual Adjustable Cable Cross", EquipmentType.CABLE_CROSSOVER, "CC-2023-004",
                "Strength Floor", "LifeFitness", "Signature MJ4", LocalDate.of(2023, 8, 12), MaintenanceStatus.PARTS_ORDERED);
        eq4.setLastInspectionDate(LocalDate.now().minusDays(8));

        Equipment eq5 = new Equipment("45-Degree Plate-Loaded Leg Press", EquipmentType.LEG_PRESS, "LP-2023-005",
                "Strength Floor", "Hammer Strength", "Plate-Loaded LP", LocalDate.of(2023, 3, 5), MaintenanceStatus.OPERATIONAL);
        eq5.setLastInspectionDate(LocalDate.now().minusDays(30));

        Equipment eq6 = new Equipment("Keiser M3i Indoor Cycle", EquipmentType.STATIONARY_BIKE, "BK-2024-006",
                "Studio 1", "Keiser", "M3i Bluetooth", LocalDate.of(2024, 1, 8), MaintenanceStatus.IN_REPAIR);
        eq6.setLastInspectionDate(LocalDate.now().minusDays(1));

        Equipment eq7 = new Equipment("Olympic Olympic Flat Bench", EquipmentType.BENCH_PRESS, "BP-2022-007",
                "Strength Floor", "Rogue Fitness", "Monster Bench", LocalDate.of(2022, 11, 1), MaintenanceStatus.OPERATIONAL);
        eq7.setLastInspectionDate(LocalDate.now().minusDays(20));

        Equipment eq8 = new Equipment("Woodway Curve Manual Treadmill", EquipmentType.TREADMILL, "TM-2023-008",
                "Functional Training Zone", "Woodway", "Curve Trainer", LocalDate.of(2023, 9, 18), MaintenanceStatus.REPORTED);
        eq8.setLastInspectionDate(LocalDate.now().minusDays(3));

        equipmentRepository.saveAll(List.of(eq1, eq2, eq3, eq4, eq5, eq6, eq7, eq8));

        // Create sample maintenance logs
        MaintenanceLog log1 = new MaintenanceLog(eq2, "Trainer Alex", "TRAINER",
                "Excessive belt squeaking and resistance glitch",
                "Resistance motor makes high-pitched whining noise when cadence exceeds 60 RPM.",
                PriorityLevel.HIGH, MaintenanceStatus.UNDER_INSPECTION);
        log1.setAssignedTechnician("Dave Miller");
        log1.setEstimatedCost(180.0);

        MaintenanceLog log2 = new MaintenanceLog(eq4, "Staff Marcus", "TRAINER",
                "Frayed left pulley steel cable",
                "Left pulley cable shows nylon coating peel and wire fraying near lower carabiner. Potential safety hazard.",
                PriorityLevel.CRITICAL_SAFETY_HAZARD, MaintenanceStatus.PARTS_ORDERED);
        log2.setAssignedTechnician("Sarah Jenkins");
        log2.setEstimatedCost(240.0);
        log2.setResolutionNotes("OEM cable assembly #LF-9921 ordered from distributor. Expected delivery 2 days.");

        MaintenanceLog log3 = new MaintenanceLog(eq6, "Instructor Elena", "TRAINER",
                "Magnetic brake flywheel binding",
                "Pedal cadence resistance stays jammed at maximum gear 24 regardless of computer setting.",
                PriorityLevel.MEDIUM, MaintenanceStatus.IN_REPAIR);
        log3.setAssignedTechnician("Dave Miller");
        log3.setEstimatedCost(95.0);

        MaintenanceLog log4 = new MaintenanceLog(eq8, "Trainer Alex", "TRAINER",
                "Front slat alignment loose",
                "Rubberized slats have shifted slightly to left track causing friction against side skirt.",
                PriorityLevel.HIGH, MaintenanceStatus.REPORTED);
        log4.setAssignedTechnician("Dave Miller");

        // A resolved log for MTTR metric calculation
        MaintenanceLog resolvedLog = new MaintenanceLog(eq1, "Trainer Alex", "TRAINER",
                "Heart rate telemetry sensor failure",
                "Left handlebar pulse grip not transmitting BPM to console.",
                PriorityLevel.LOW, MaintenanceStatus.OPERATIONAL);
        resolvedLog.setAssignedTechnician("Sarah Jenkins");
        resolvedLog.setEstimatedCost(50.0);
        resolvedLog.setActualCost(45.0);
        resolvedLog.setReportedAt(LocalDateTime.now().minusDays(4));
        resolvedLog.setResolvedAt(LocalDateTime.now().minusDays(3).minusHours(18));
        resolvedLog.setResolutionNotes("Grip sensor contact cleaned and wiring harness reseated. Verified with Polar chest strap.");

        maintenanceLogRepository.saveAll(List.of(log1, log2, log3, log4, resolvedLog));
    }
}
