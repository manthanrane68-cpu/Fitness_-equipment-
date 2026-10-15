# ==============================================================================
# Multi-stage Dockerfile for Puppet-Provisioned Fitness Equipment Maintenance Portal
# ==============================================================================

# Stage 1: Build stage with OpenJDK 21 and Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency descriptor first for efficient layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Package production executable JAR without running tests inside container build
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal hardened runtime image
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DevOps Team <devops@fitnessequipment.local>"
LABEL description="Fitness Equipment Maintenance Portal Container"
LABEL version="1.0.0"

# Create non-root system group and user
RUN addgroup -S fitnessapp && adduser -S fitnessapp -G fitnessapp

# Setup application directory structure
WORKDIR /app
RUN mkdir -p /app/logs && chown -R fitnessapp:fitnessapp /app

# Copy artifact from builder stage
COPY --from=builder --chown=fitnessapp:fitnessapp /build/target/fitness-equipment-portal.jar /app/fitness-equipment-portal.jar

# Switch to non-privileged user
USER fitnessapp

# Expose HTTP application port
EXPOSE 8080

# Environment variables
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Container healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Launch Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/fitness-equipment-portal.jar"]
