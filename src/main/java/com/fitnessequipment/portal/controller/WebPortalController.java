package com.fitnessequipment.portal.controller;

import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import com.fitnessequipment.portal.model.PriorityLevel;
import com.fitnessequipment.portal.service.DashboardService;
import com.fitnessequipment.portal.service.EquipmentService;
import com.fitnessequipment.portal.service.MaintenanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebPortalController {

    private final EquipmentService equipmentService;
    private final MaintenanceService maintenanceService;
    private final DashboardService dashboardService;

    public WebPortalController(EquipmentService equipmentService,
                               MaintenanceService maintenanceService,
                               DashboardService dashboardService) {
        this.equipmentService = equipmentService;
        this.maintenanceService = maintenanceService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("summary", dashboardService.getDashboardSummary());
        model.addAttribute("equipmentList", equipmentService.getAllEquipment());
        model.addAttribute("activeBreakdowns", maintenanceService.getActiveBreakdowns());
        model.addAttribute("allLogs", maintenanceService.getAllLogs());
        model.addAttribute("types", EquipmentType.values());
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("priorities", PriorityLevel.values());
        model.addAttribute("zones", equipmentService.getAllGymZones());
        return "index";
    }

    @GetMapping("/equipment")
    public String equipmentPage(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EquipmentType type,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) String zone,
            Model model) {

        model.addAttribute("equipmentList", equipmentService.searchEquipment(query, type, status, zone));
        model.addAttribute("types", EquipmentType.values());
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("zones", equipmentService.getAllGymZones());
        model.addAttribute("query", query);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedZone", zone);
        return "equipment-list";
    }

    @GetMapping("/maintenance")
    public String maintenancePage(
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) PriorityLevel priority,
            Model model) {

        model.addAttribute("logs", maintenanceService.filterLogs(status, priority));
        model.addAttribute("equipmentList", equipmentService.getAllEquipment());
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("priorities", PriorityLevel.values());
        return "maintenance-workflow";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("summary", dashboardService.getDashboardSummary());
        return "dashboard";
    }
}
