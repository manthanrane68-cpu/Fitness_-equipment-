@echo off
REM ============================================================================
REM Fitness Equipment Maintenance Portal - Local Development Startup
REM ============================================================================

echo [INFO] Starting Fitness Equipment Maintenance Portal in Local Dev Mode...
echo [INFO] Port: 8080
echo [INFO] Health Check: http://localhost:8080/api/dashboard/health
echo [INFO] H2 Database Console: http://localhost:8080/h2-console
echo [INFO] Web UI: http://localhost:8080/

mvn spring-boot:run
