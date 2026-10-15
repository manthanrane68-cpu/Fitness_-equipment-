package com.fitnessequipment.portal.service;

import com.fitnessequipment.portal.dto.EquipmentRequestDto;
import com.fitnessequipment.portal.model.Equipment;
import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import com.fitnessequipment.portal.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    public Optional<Equipment> getEquipmentBySerialNumber(String serialNumber) {
        return equipmentRepository.findBySerialNumber(serialNumber);
    }

    public List<Equipment> searchEquipment(String query, EquipmentType type, MaintenanceStatus status, String zone) {
        return equipmentRepository.searchEquipment(query, type, status, zone);
    }

    public List<String> getAllGymZones() {
        return equipmentRepository.findDistinctGymZones();
    }

    public Equipment createEquipment(EquipmentRequestDto dto) {
        Equipment equipment = new Equipment(
                dto.getName(),
                dto.getType(),
                dto.getSerialNumber(),
                dto.getGymZone(),
                dto.getManufacturer(),
                dto.getModelNumber(),
                dto.getInstallationDate(),
                dto.getCurrentStatus() != null ? dto.getCurrentStatus() : MaintenanceStatus.OPERATIONAL
        );
        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(Long id, EquipmentRequestDto dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + id));

        equipment.setName(dto.getName());
        equipment.setType(dto.getType());
        equipment.setSerialNumber(dto.getSerialNumber());
        equipment.setGymZone(dto.getGymZone());
        equipment.setManufacturer(dto.getManufacturer());
        equipment.setModelNumber(dto.getModelNumber());
        if (dto.getInstallationDate() != null) {
            equipment.setInstallationDate(dto.getInstallationDate());
        }
        if (dto.getCurrentStatus() != null) {
            equipment.setCurrentStatus(dto.getCurrentStatus());
        }
        return equipmentRepository.save(equipment);
    }

    public void updateStatus(Long id, MaintenanceStatus newStatus) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + id));
        equipment.setCurrentStatus(newStatus);
        equipmentRepository.save(equipment);
    }

    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }
}
