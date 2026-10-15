package com.fitnessequipment.portal.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnessequipment.portal.dto.EquipmentRequestDto;
import com.fitnessequipment.portal.model.EquipmentType;
import com.fitnessequipment.portal.model.MaintenanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipmentApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/equipment returns 200 and list of equipment")
    void testGetAllEquipment() throws Exception {
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/equipment creates new asset and returns 201")
    void testCreateEquipmentApi() throws Exception {
        EquipmentRequestDto dto = new EquipmentRequestDto();
        dto.setName("API Test Treadmill");
        dto.setType(EquipmentType.TREADMILL);
        dto.setSerialNumber("API-SN-8888");
        dto.setGymZone("Cardio Hub");
        dto.setCurrentStatus(MaintenanceStatus.OPERATIONAL);

        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.serialNumber").value("API-SN-8888"));
    }

    @Test
    @DisplayName("GET /api/dashboard/summary returns KPI data")
    void testDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEquipment").isNumber())
                .andExpect(jsonPath("$.operationalCount").isNumber())
                .andExpect(jsonPath("$.activeBreakdowns").isNumber());
    }

    @Test
    @DisplayName("GET /api/dashboard/health returns UP status")
    void testDashboardHealth() throws Exception {
        mockMvc.perform(get("/api/dashboard/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
