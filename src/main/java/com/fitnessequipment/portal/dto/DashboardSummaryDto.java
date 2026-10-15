package com.fitnessequipment.portal.dto;

import java.util.Map;

public class DashboardSummaryDto {

    private long totalEquipment;
    private long operationalCount;
    private long underInspectionCount;
    private long partsOrderedCount;
    private long inRepairCount;
    private long activeBreakdowns;
    private long criticalSafetyHazards;
    private double meanTimeToRepairHours;
    private Map<String, Long> countByType;
    private Map<String, Long> countByZone;

    public DashboardSummaryDto() {
    }

    public long getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(long totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public long getOperationalCount() {
        return operationalCount;
    }

    public void setOperationalCount(long operationalCount) {
        this.operationalCount = operationalCount;
    }

    public long getUnderInspectionCount() {
        return underInspectionCount;
    }

    public void setUnderInspectionCount(long underInspectionCount) {
        this.underInspectionCount = underInspectionCount;
    }

    public long getPartsOrderedCount() {
        return partsOrderedCount;
    }

    public void setPartsOrderedCount(long partsOrderedCount) {
        this.partsOrderedCount = partsOrderedCount;
    }

    public long getInRepairCount() {
        return inRepairCount;
    }

    public void setInRepairCount(long inRepairCount) {
        this.inRepairCount = inRepairCount;
    }

    public long getActiveBreakdowns() {
        return activeBreakdowns;
    }

    public void setActiveBreakdowns(long activeBreakdowns) {
        this.activeBreakdowns = activeBreakdowns;
    }

    public long getCriticalSafetyHazards() {
        return criticalSafetyHazards;
    }

    public void setCriticalSafetyHazards(long criticalSafetyHazards) {
        this.criticalSafetyHazards = criticalSafetyHazards;
    }

    public double getMeanTimeToRepairHours() {
        return meanTimeToRepairHours;
    }

    public void setMeanTimeToRepairHours(double meanTimeToRepairHours) {
        this.meanTimeToRepairHours = meanTimeToRepairHours;
    }

    public Map<String, Long> getCountByType() {
        return countByType;
    }

    public void setCountByType(Map<String, Long> countByType) {
        this.countByType = countByType;
    }

    public Map<String, Long> getCountByZone() {
        return countByZone;
    }

    public void setCountByZone(Map<String, Long> countByZone) {
        this.countByZone = countByZone;
    }
}
