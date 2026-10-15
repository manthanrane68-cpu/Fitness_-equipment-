package com.fitnessequipment.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Equipment name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Equipment type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    @NotBlank(message = "Serial number is required")
    @Column(nullable = false, unique = true)
    private String serialNumber;

    @NotBlank(message = "Gym zone / location is required")
    @Column(nullable = false)
    private String gymZone;

    private String manufacturer;

    private String modelNumber;

    private LocalDate installationDate;

    private LocalDate lastInspectionDate;

    @NotNull(message = "Current status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus currentStatus = MaintenanceStatus.OPERATIONAL;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MaintenanceLog> maintenanceLogs = new ArrayList<>();

    public Equipment() {
    }

    public Equipment(String name, EquipmentType type, String serialNumber, String gymZone,
                     String manufacturer, String modelNumber, LocalDate installationDate,
                     MaintenanceStatus currentStatus) {
        this.name = name;
        this.type = type;
        this.serialNumber = serialNumber;
        this.gymZone = gymZone;
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
        this.installationDate = installationDate;
        this.currentStatus = currentStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDate lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public MaintenanceStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(MaintenanceStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MaintenanceLog> getMaintenanceLogs() {
        return maintenanceLogs;
    }

    public void setMaintenanceLogs(List<MaintenanceLog> maintenanceLogs) {
        this.maintenanceLogs = maintenanceLogs;
    }
}
