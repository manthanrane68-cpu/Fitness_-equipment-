@echo off
REM ============================================================================
REM Selenium Automated Regression Test Suite Runner
REM ============================================================================

echo [INFO] Running Selenium User Journeys with JUnit 5 & Headless Chrome/Edge...
echo [INFO] Failure screenshots will be automatically captured in target/screenshots/

mvn test -Dtest=*SeleniumTest,*JourneyTest

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Selenium Regression Suite FAILED! Inspect target/screenshots/
    exit /b %ERRORLEVEL%
) else (
    echo [SUCCESS] All Selenium User Journeys PASSED!
)
