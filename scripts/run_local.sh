#!/usr/bin/env bash
# ==============================================================================
# Fitness Equipment Maintenance Portal - Unix / Linux Startup Script
# ==============================================================================
set -euo pipefail

echo "========================================================================"
echo " Starting Fitness Equipment Maintenance Portal (Java 21 / Spring Boot 3)"
echo "========================================================================"
echo " Local UI:      http://localhost:8080/"
echo " Dashboard:     http://localhost:8080/dashboard"
echo " Health API:    http://localhost:8080/api/dashboard/health"
echo " Actuator:      http://localhost:8080/actuator/health"
echo " H2 Console:    http://localhost:8080/h2-console"
echo "========================================================================"

mvn spring-boot:run
