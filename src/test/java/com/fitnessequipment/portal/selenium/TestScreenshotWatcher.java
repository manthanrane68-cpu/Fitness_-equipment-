package com.fitnessequipment.portal.selenium;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TestScreenshotWatcher implements TestWatcher {

    private static WebDriver driver;

    public static void setDriver(WebDriver webDriver) {
        driver = webDriver;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (driver != null && driver instanceof TakesScreenshot) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String testName = context.getRequiredTestClass().getSimpleName() + "_" +
                                  context.getRequiredTestMethod().getName();
                String dirPath = "target/screenshots";
                Files.createDirectories(Paths.get(dirPath));

                File screenshotFile = new File(dirPath + "/" + testName + "_" + timestamp + "_FAILED.png");
                byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

                try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
                    fos.write(screenshotBytes);
                }
                System.err.println("[SELENIUM TEST FAILURE] Screenshot captured at: " + screenshotFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Could not save failure screenshot: " + e.getMessage());
            }
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("[SELENIUM TEST SUCCESS] " + context.getDisplayName() + " passed.");
    }
}
