package com.fitnessequipment.portal.dto;

import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class EquipmentRequestDto {

    @NotBlank(message = "Equipment name is required")
    private String name;

    @NotNull(message = "Equipment type is required")
    private EquipmentType type;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Gym zone is required")
    private String gymZone;

    private String manufacturer;
    private String modelNumber;
    private LocalDate installationDate;
    private MaintenanceStatus currentStatus = MaintenanceStatus.OPERATIONAL;

    public EquipmentRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getGymZone() {
        return gymZone;
    }

    public void setGymZone(String gymZone) {
        this.gymZone = gymZone;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public MaintenanceStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(MaintenanceStatus currentStatus) {
        this.currentStatus = currentStatus;
    }
}
