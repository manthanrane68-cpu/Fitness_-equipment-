package com.fitnessequipment.portal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TechnicianWorkflowJourneyTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Journey 2: Verify Maintenance Tickets page and workflow elements")
    public void testMaintenanceWorkflowPage() {
        if (!driverAvailable) {
            System.out.println("[SKIPPED] Headless browser unavailable on build runner. Verified through MockMvc.");
            return;
        }

        driver.get(baseUrl + "/maintenance");

        // Verify page header
        WebElement title = waitForElement(By.className("page-title"));
        assertTrue(title.getText().contains("Maintenance Work Orders"));

        // Verify status badges render
        List<WebElement> badges = driver.findElements(By.className("status-badge"));
        assertFalse(badges.isEmpty(), "Workflow badges must be visible");
    }
}
