# 03. Requirements, Architecture and Technology Setup

## 1. Software Requirements Specification (SRS) Summary

### 1.1 Functional Requirements (FR)
* **FR-01: Asset Registration & Cataloging**: System must allow adding, updating, viewing, and deleting fitness equipment with unique serial numbers, manufacturer, model, gym zone, and installation date.
* **FR-02: Maintenance Incident Logging**: Floor trainers and staff must be able to log equipment defects with title, description, and priority level (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL_SAFETY_HAZARD`).
* **FR-03: Role-Governed Lifecycle Workflow**: Maintenance tickets must progress through a 5-stage pipeline: `REPORTED` &rarr; `UNDER_INSPECTION` &rarr; `PARTS_ORDERED` &rarr; `IN_REPAIR` &rarr; `OPERATIONAL` / `DECOMMISSIONED`. Non-admin roles are restricted from decommissioning assets.
* **FR-04: Multi-Parameter Search & Filter**: Users can filter equipment inventory by equipment type, operational status, gym zone, or search query string.
* **FR-05: Executive Reliability Dashboard**: System computes and renders real-time metrics: Total Fleet Units, Operational Units, Active Breakdowns, Safety Hazard Count, and Mean Time to Repair (MTTR) in hours.
* **FR-06: System Health Check**: Actuator endpoint `/actuator/health` and `/api/dashboard/health` must return `UP` status for automated orchestrators and reverse proxies.

### 1.2 Non-Functional Requirements (NFR)
* **NFR-01: Availability & Portability**: Runs seamlessly inside lightweight Docker containers or on Linux nodes provisioned via Puppet.
* **NFR-02: Performance**: Sub-2-second server rendering for all web views and sub-100ms response for JSON REST APIs under standard operational load.
* **NFR-03: Security & Non-Root Execution**: Runs under restricted OS user `fitnessapp` without root privileges in production.
* **NFR-04: Automated Verification**: 100% of critical user journeys automated via Selenium WebDriver running in headless browser mode.

---

## 2. Use Case Diagram

```mermaid
usecaseDiagram
    actor "Floor Staff / Trainer" as Staff
    actor "Maintenance Technician" as Tech
    actor "Operations Manager" as Manager
    actor "Facility Admin" as Admin

    package "Fitness Equipment Maintenance Portal" {
        usecase "UC-01: Register Equipment Asset" as UC1
        usecase "UC-02: Search & Filter Fleet by Zone/Type" as UC2
        usecase "UC-03: Log Equipment Breakdown Ticket" as UC3
        usecase "UC-04: Diagnose & Transition Workflow" as UC4
        usecase "UC-05: Record Spares Cost & Notes" as UC5
        usecase "UC-06: Decommission Unrepairable Asset" as UC6
        usecase "UC-07: Monitor MTTR & Reliability KPIs" as UC7
        usecase "UC-08: Health Check & Actuator Telemetry" as UC8
    }

    Staff --> UC2
    Staff --> UC3

    Tech --> UC2
    Tech --> UC4
    Tech --> UC5

    Manager --> UC1
    Manager --> UC2
    Manager --> UC3
    Manager --> UC7

    Admin --> UC1
    Admin --> UC6
    Admin --> UC7
    Admin --> UC8
```

---

## 3. High-Level System Architecture

```mermaid
flowchart TD
    subgraph Client_Layer["Presentation Layer (Client Browsers & Devices)"]
        Browser["Responsive HTML5 / CSS3 / Vanilla JS Web Client"]
        Mobile["Floor Staff Mobile Tablets"]
    end

    subgraph Reverse_Proxy_Layer["Ingress & Gateway (Nginx)"]
        Nginx["Nginx Reverse Proxy (Port 80)\n- SSL Termination\n- Static Caching\n- Gzip Compression"]
    end

    subgraph Application_Layer["Core Application Engine (Spring Boot 3 / Java 21)"]
        Tomcat["Embedded / Standalone Tomcat Server (Port 8080)"]
        Controllers["Spring MVC Controllers & REST APIs\n- WebPortalController\n- EquipmentApiController\n- MaintenanceApiController\n- DashboardApiController"]
        Services["Domain Business Logic\n- EquipmentService\n- MaintenanceService\n- DashboardService (MTTR Calculator)"]
        Security["Role-Based Workflow Enforcement Gate"]
    end

    subgraph Persistence_Layer["Data & Persistence"]
        JPA["Spring Data JPA / Hibernate ORM"]
        H2["H2 Database Engine\n- In-Memory for CI/CD Testing\n- Persistent File-Backed for Puppet Node"]
    end

    subgraph Ops_Layer["DevOps Orchestration & Observability"]
        Actuator["Spring Boot Actuator\n(/actuator/health)"]
        Puppet["Puppet Agent (Configuration Management)"]
        Docker["Docker Engine & Container Runtime"]
        Jenkins["Jenkins CI/CD Automation Server"]
    end

    Browser -->|HTTP Port 80| Nginx
    Mobile -->|HTTP Port 80| Nginx
    Nginx -->|Reverse Proxy Port 8080| Tomcat
    Tomcat --> Controllers
    Controllers --> Security
    Security --> Services
    Services --> JPA
    JPA --> H2
    Jenkins --> Docker
    Jenkins --> Puppet
    Puppet --> Tomcat
    Actuator --> Nginx
```

---

## 4. Data Model (Entity-Relationship Diagram)

```mermaid
erDiagram
    EQUIPMENT ||--o{ MAINTENANCE_LOG : "has historical logs"

    EQUIPMENT {
        Long id PK "Auto-increment identifier"
        String name "e.g. ProRunner Treadmill X9"
        EquipmentType type "TREADMILL, ELLIPTICAL, etc."
        String serialNumber UK "Unique manufacturer serial"
        String gymZone "e.g. Cardio Zone A, Strength Floor"
        String manufacturer "e.g. LifeFitness, Matrix"
        String modelNumber "e.g. PRX9-PRO"
        LocalDate installationDate "Asset commission date"
        LocalDate lastInspectionDate "Preventive check date"
        MaintenanceStatus currentStatus "OPERATIONAL, REPORTED, etc."
        LocalDateTime createdAt "Creation timestamp"
        LocalDateTime updatedAt "Last update timestamp"
    }

    MAINTENANCE_LOG {
        Long id PK "Auto-increment ticket identifier"
        Long equipment_id FK "References equipment.id"
        String reportedBy "Staff member / trainer name"
        String reporterRole "TRAINER, TECHNICIAN, ADMIN"
        String issueTitle "Concise symptom summary"
        String issueDescription "Detailed diagnostic observation"
        PriorityLevel priority "LOW, MEDIUM, HIGH, CRITICAL_SAFETY_HAZARD"
        MaintenanceStatus status "REPORTED -> INSPECTED -> REPAIR -> OPERATIONAL"
        String assignedTechnician "Technician responsible"
        Double estimatedCost "Estimated parts / labor"
        Double actualCost "Final repair invoice cost"
        String resolutionNotes "Remediation audit trail notes"
        LocalDateTime reportedAt "Incident creation time"
        LocalDateTime resolvedAt "Completion timestamp"
        LocalDateTime updatedAt "Last status change time"
    }
```

---

## 5. REST API Specifications

| Method | Endpoint | Description | Request Body / Query Params | Success Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/equipment` | Retrieve all or filtered equipment | `query`, `type`, `status`, `zone` | `200 OK` (JSON Array) |
| `GET` | `/api/equipment/{id}` | Get single equipment asset by ID | Path variable `id` | `200 OK` / `404 Not Found` |
| `GET` | `/api/equipment/by-serial/{sn}` | Lookup machine by serial number | Path variable `sn` | `200 OK` / `404 Not Found` |
| `GET` | `/api/equipment/zones` | Distinct list of gym zones | None | `200 OK` (List of Strings) |
| `POST` | `/api/equipment` | Register new equipment asset | `EquipmentRequestDto` (JSON) | `201 Created` (Equipment JSON) |
| `PUT` | `/api/equipment/{id}` | Update equipment asset | `EquipmentRequestDto` (JSON) | `200 OK` (Equipment JSON) |
| `DELETE`| `/api/equipment/{id}` | Delete equipment asset | Path variable `id` | `204 No Content` |
| `GET` | `/api/maintenance` | Retrieve all maintenance tickets | `status`, `priority` | `200 OK` (List of Logs) |
| `GET` | `/api/maintenance/active` | Active breakdown tickets only | None | `200 OK` (Active Breakdowns) |
| `POST` | `/api/maintenance` | Submit new breakdown report | `MaintenanceRequestDto` (JSON) | `201 Created` (Log JSON) |
| `PATCH`| `/api/maintenance/{id}/workflow` | Transition ticket workflow state | `{status, role, technician, notes, actualCost}` | `200 OK` / `403 Forbidden` |
| `GET` | `/api/dashboard/summary` | Executive KPI analytics metrics | None | `200 OK` (DashboardSummaryDto) |
| `GET` | `/api/dashboard/health` | Application health ping | None | `200 OK` (`{"status":"UP"}`) |

---

## 6. Local Development Setup Guide

### Prerequisites
* Java Development Kit (JDK 21 or higher)
* Apache Maven (3.9.x or higher)
* Modern Web Browser (Chrome, Edge, Firefox)

### Step-by-Step Execution
1. **Clone the repository**:
   ```bash
   git clone https://github.com/manthanrane68-cpu/Fitness_-equipment-.git
   cd Fitness_-equipment-
   ```
2. **Compile and execute automated test suite**:
   ```bash
   mvn clean test
   ```
3. **Run the local application**:
   ```bash
   mvn spring-boot:run
   ```
4. **Access the portal**:
   * Web Portal UI: `http://localhost:8080/`
   * Equipment Fleet: `http://localhost:8080/equipment`
   * Maintenance Workflow: `http://localhost:8080/maintenance`
   * Executive Dashboard: `http://localhost:8080/dashboard`
   * H2 Database Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:fitnessdb`, User: `sa`, Password: `password`)
   * Health Endpoint: `http://localhost:8080/api/dashboard/health`
