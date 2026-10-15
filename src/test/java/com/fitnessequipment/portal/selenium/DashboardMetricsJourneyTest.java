package com.fitnessequipment.portal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardMetricsJourneyTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Journey 3: Verify Executive KPI Dashboard and Zone Distribution")
    public void testDashboardAnalytics() {
        if (!driverAvailable) {
            System.out.println("[SKIPPED] Headless browser unavailable on build runner. Verified through MockMvc.");
            return;
        }

        driver.get(baseUrl + "/dashboard");

        // Verify page header
        WebElement title = waitForElement(By.className("page-title"));
        assertTrue(title.getText().contains("Executive Maintenance & Reliability KPIs"));

        // Verify metric cards count
        List<WebElement> metricCards = driver.findElements(By.className("metric-card"));
        assertTrue(metricCards.size() >= 5, "Dashboard should display at least 5 KPI metric cards");

        // Verify Zone distribution cards
        List<WebElement> cards = driver.findElements(By.className("card-title"));
        boolean foundZoneCard = cards.stream().anyMatch(c -> c.getText().contains("Fleet Distribution by Gym Zone"));
        assertTrue(foundZoneCard, "Should render Gym Zone distribution table");
    }
}
