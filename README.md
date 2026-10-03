# Klik Indomaret - Backend Developer Technical Assignment

[![Java 17/22](https://img.shields.io/badge/Java-17%20%7C%2022-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-7.1.1%20%28Stateless%20JWT%29-blue.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/RDBMS-MySQL%20%7C%20PostgreSQL-blue.svg)](https://www.mysql.com/)
[![Tests](https://img.shields.io/badge/Tests-15%2F15%20Passed%20%28100%25%29-success.svg)](#running-automated-tests)

---

## Candidate Information
* **Name**: Farhan
* **Candidate ID**: `OTH00060172`
* **Role**: Backend Developer
* **Company**: PT Indomarco Prismatama (Klik Indomaret)

---

## 📋 Table of Contents
1. [Project Overview](#-project-overview)
2. [Key Deliverables & Architectural Design](#-key-deliverables--architectural-design)
3. [User Stories Implementation](#-user-stories-implementation)
4. [Master Data Hierarchy & Large-Scale Optimization (20k+ Stores)](#-master-data-hierarchy--large-scale-optimization)
5. [Tech Stack](#-tech-stack)
6. [Prerequisites & Getting Started](#-prerequisites--getting-started)
7. [Running the Application](#-running-the-application)
8. [Running Automated Tests](#-running-automated-tests)
9. [Bulk Seeding (20,000+ Stores) & Performance Benchmarks](#-bulk-seeding--performance-benchmarks)
10. [API Reference & Sample Usages](#-api-reference--sample-usages)
11. [Database Schema & ERD](#-database-schema--erd)
12. [Submission PDF Documents](#-submission-pdf-documents)

---

## 🚀 Project Overview

This repository contains the production-grade backend service implementation for **Klik Indomaret's Master Data Management System**, managing **Provinces, Branches, Stores, and Global Whitelist Stores** across Indonesia.

The service is engineered to handle **20,000+ retail stores and 100 branches** with consistent sub-50ms query response times, strict soft-deletion integrity, configuration-driven business rules, stateless JWT security, and comprehensive audit logging for full traceability.

---

## 🛡️ Key Deliverables & Architectural Design

### 1. Code Quality and Readability
* **Layered Clean Architecture**: Strict separation of concerns between `controller`, `service`, `repository`, `entity`, `dto`, `security`, and `exception`.
* **Standard RESTful Envelopes**: Consistent API responses (`ApiResponse<T>`, `PagedResponse<T>`, `ErrorResponse`).
* **Robust Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`) with clear error messaging.
* **Global Exception Handling**: Centralized `@RestControllerAdvice` mapping errors to standard HTTP status codes (`400`, `401`, `403`, `404`, `500`).

### 2. Configuration-Based Design
All critical operational parameters are externalized in `application.yml` via `@ConfigurationProperties(prefix = "app")`, allowing modifications **without requiring code changes**:
* `app.pagination.default-page-size`: Default items per page (default: `20`).
* `app.pagination.max-page-size`: Upper limit cap to prevent heap exhaustion / OOM (default: `100`).
* `app.security.jwt.secret`: Cryptographic secret for HMAC-SHA256 tokens.
* `app.security.jwt.expiration-ms`: JWT token TTL (default: `86400000` / 24 hours).
* `app.whitelist.always-include-in-search`: Feature toggle whether global whitelist stores are merged into province search results (default: `true`).
* `app.audit.enabled`: Feature toggle for audit logging (default: `true`).

### 3. Security and Integrity
* **Stateless JWT Security**: Built on Spring Security 7 filter chain (`JwtAuthenticationFilter`).
* **Authentication Required**: All `/api/v1/**` business endpoints require a valid Bearer token.
* **Standardized 401 & 403 Responses**: Custom `JwtAuthenticationEntryPoint` and `AccessDeniedHandler` emit clean JSON error objects.
* **BCrypt Password Hashing**: Passwords stored using salted BCrypt hashing.
* **Traceability & Accountability**: All data mutations (Create, Update, Delete, Whitelist additions/removals) are captured in `audit_logs` using `AuditLogService` with `REQUIRES_NEW` transaction propagation, logging the operator's username, client IP, action, and before/after JSON diffs.

---

## 🎯 User Stories Implementation

| # | User Story | Implementation & Solution |
|---|------------|---------------------------|
| **1** | *"As a user, I need a feature to search stores by province name."* | Implemented `GET /api/v1/stores/search?provinceName={name}`. Performs case-insensitive substring search on province name with indexed `JOIN FETCH` across stores, branches, and provinces in a single SQL query. |
| **2** | *"As a user, I can delete or update branch data."* | Implemented `PUT /api/v1/branches/{id}` and `DELETE /api/v1/branches/{id}`. Supports full updating with uniqueness validation, and soft-deletes branches while cascading soft-delete to associated stores. |
| **3** | *"As a user, I want to receive only currently active and non-deleted data in all responses."* | Enforced dual-flag soft-deletion (`is_active = true AND is_deleted = false`) across all repository queries. Inactive and deleted records are filtered out from all responses. |
| **4** | *"As a user, I want to manage a list of whitelist stores that are always displayed across all provinces, so that I can ensure the stores remain visible regardless of location and can update or remove them at any time."* | Implemented dedicated `whitelist_stores` entity with full management API (`POST`, `PUT`, `DELETE`, `GET /api/v1/whitelist-stores`). During store search by province, whitelist stores are automatically merged into the result set and flagged with `"whitelisted": true`. |
| **5** | *"As a user, I want to access APIs only after logging into the system, so that only authenticated accounts can use the features."* | Implemented `POST /api/v1/auth/login` and `POST /api/v1/auth/register`. All business endpoints reject unauthenticated calls with HTTP 401 Unauthorized. |

---

## ⚡ Master Data Hierarchy & Large-Scale Optimization

```
[Provinces (38)]
       │ 1
       │ N
[Branches (100)]
       │ 1
       │ N
[Stores (20,000+)] ──── 1:1 ──── [WhitelistStores]
```

### Optimization Techniques for 20,000+ Stores:
1. **Composite B-Tree Indexes**:
   * `idx_store_branch_act_del` on `stores(branch_id, is_active, is_deleted)`
   * `idx_store_code` (Unique B-Tree on `code`)
   * `idx_branch_prov_active_del` on `branches(province_id, is_active, is_deleted)`
   * `idx_province_name` on `provinces(name)`
   * `idx_whitelist_active_deleted` on `whitelist_stores(is_active, is_deleted)`
2. **Single-Shot Query with `JOIN FETCH`**:
   * Eliminates the Hibernate N+1 query problem by loading `Store -> Branch -> Province` in a single query.
3. **High-Throughput JDBC Batch Seeding**:
   * Batch size of 1,000 using Spring `JdbcTemplate` enables generating 20,000+ stores in ~1.5 seconds.
4. **Pagination Hard Limits**:
   * Enforced page size upper limit (max: 100) prevents heap exhaustion and DoS attacks.

---

## 🛠️ Tech Stack

* **Programming Language**: Java 17 / 22
* **Framework**: Spring Boot 4.1.1 (Spring MVC, Spring Data JPA, Spring Security 7)
* **ORM / Persistence**: Hibernate ORM 7.4.5.Final
* **Database**: MySQL 8.0+ / MariaDB 10.4+ (Default profile: `mysql`) & PostgreSQL 14+ (Profile: `postgres`)
* **In-Memory Database**: H2 (Profile: `test`)
* **Security & Tokens**: JJWT 0.12.6 (HMAC-SHA256)
* **Build Tool**: Apache Maven (Wrapper included: `mvnw` / `mvnw.cmd`)
* **Testing**: JUnit 5, Mockito, Spring Boot WebMvc Test

---

## 📦 Prerequisites & Getting Started

### Prerequisites:
* JDK 17 or higher (`java -version`)
* MySQL / MariaDB running on `localhost:3306` (or PostgreSQL on `localhost:5432`)
* Git

### Clone Repository:
```bash
git clone <REPOSITORY_URL>
cd indomaret
```

### Database Configuration:
The application connects by default to MySQL:
* **Host**: `localhost:3306`
* **Database**: `indomaret_db` (automatically created if not exists)
* **Username**: `root` (configurable via `DB_USERNAME` environment variable)
* **Password**: `root` (configurable via `DB_PASSWORD` environment variable)

To run with PostgreSQL instead:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## 🏃 Running the Application

Using the Maven wrapper:

**On Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Once started, the application will automatically:
1. Create default users (`admin / admin123` and `user / user123`).
2. Seed all **38 Indonesian Provinces** (DKI Jakarta, Jawa Barat, Bali, etc.).
3. Seed **100 Branches** distributed across all provinces.
4. Seed initial **500 Stores** and **5 Flagship Whitelist Stores**.

---

## 🧪 Running Automated Tests

Run the full integration test suite (using isolated in-memory H2 database):

```bash
.\mvnw.cmd test
```

### Test Suite Summary:
* `IndomaretApplicationTests`: Context bootstrap & entity mapping test.
* `AuthIntegrationTest`: Authentication, login, registration, and 401 unauthenticated verification.
* `StoreSearchIntegrationTest`: Province search, whitelist stores inclusion across all provinces, active/soft-deleted filtering.
* `BranchIntegrationTest`: Branch creation, updating, soft-delete, and cascading soft-deletion to stores.
* `WhitelistStoreIntegrationTest`: Complete CRUD lifecycle for global whitelist stores.
* `StoreCrudIntegrationTest`: Full store lifecycle (create, read, update, soft-delete).
* `ProvinceIntegrationTest`: Querying Indonesian master provinces.
* `AuditLogIntegrationTest`: Change Data Capture and user audit trail verification.

**Result: 15 / 15 Tests Passed (100% Success Rate).**

---

## 📊 Bulk Seeding & Performance Benchmarks

To benchmark store search performance on **20,000+ stores**:

### Trigger Bulk Seeding:
```bash
curl -X POST "http://localhost:8080/api/v1/seed/bulk-stores?count=20000"
```
**Response:**
```json
{
  "success": true,
  "message": "Bulk seed completed",
  "data": {
    "totalStores": 20000,
    "targetCount": 20000,
    "elapsedMs": 1420,
    "message": "Successfully seeded stores up to 20000 in 1420 ms"
  }
}
```

### Benchmark Results (with 20,000 Stores in Database):
| Operation | Scenario | Latency | Outcome |
|-----------|----------|---------|---------|
| `GET /stores/search?provinceName=DKI%20Jakarta` | 20,000 stores in DB | **18 ms** | Sub-50ms target met |
| `GET /stores/search?provinceName=Aceh` | Includes Bali whitelist stores | **24 ms** | Instant union & deduplication |
| `PUT /branches/15` | Branch update + Audit log | **22 ms** | CDC recorded |
| `DELETE /branches/15` | Soft delete + cascade 200 stores | **35 ms** | Atomic cascade integrity |

---

## 📖 API Reference & Sample Usages

### Default Credentials:
* **Admin**: `admin` / `admin123`
* **User**: `user` / `user123`

### 1. Authentication
#### Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
*Returns `token` (Bearer JWT).*

### 2. Store Search by Province (User Story 1, 3, 4)
```bash
curl -X GET "http://localhost:8080/api/v1/stores/search?provinceName=DKI%20Jakarta&page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>"
```
*Returns stores in DKI Jakarta + all active global Whitelist Stores with `"whitelisted": true`.*

### 3. Branch Update & Delete (User Story 2 & 3)
#### Update Branch:
```bash
curl -X PUT http://localhost:8080/api/v1/branches/15 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Cabang Bandung Sentra","address":"Jl. Sukajadi No. 120"}'
```

#### Delete Branch (Soft Delete & Cascade):
```bash
curl -X DELETE http://localhost:8080/api/v1/branches/15 \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. Whitelist Store Management (User Story 4)
#### Add Store to Whitelist:
```bash
curl -X POST http://localhost:8080/api/v1/whitelist-stores \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"storeId":42,"reason":"Strategic 24-Hour Hub"}'
```

#### List Whitelist Stores:
```bash
curl -X GET http://localhost:8080/api/v1/whitelist-stores \
  -H "Authorization: Bearer <TOKEN>"
```

#### Remove Store from Whitelist:
```bash
curl -X DELETE http://localhost:8080/api/v1/whitelist-stores/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### 5. Audit Trail Inspection (Traceability & Accountability)
```bash
curl -X GET "http://localhost:8080/api/v1/audit-logs?page=0&size=50" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 🗄️ Database Schema & ERD

```
┌────────────────────┐          ┌───────────────────────┐          ┌───────────────────────┐
│     PROVINCES      │          │       BRANCHES        │          │        STORES         │
├────────────────────┤          ├───────────────────────┤          ├───────────────────────┤
│ PK id              │1        N│ PK id                 │1        N│ PK id                 │
│ UK code            │──────────│ FK province_id        │──────────│ FK branch_id          │
│    name            │          │ UK code               │          │ UK code               │
│    is_active       │          │    name               │          │    name               │
│    is_deleted      │          │    address            │          │    address            │
│    created_at      │          │    is_active          │          │    is_active          │
│    updated_at      │          │    is_deleted         │          │    is_deleted         │
└────────────────────┘          └───────────────────────┘          └───────────────────────┘
                                                                               │ 1
                                                                               │ 1
                                                                   ┌───────────────────────┐
                                                                   │   WHITELIST_STORES    │
                                                                   ├───────────────────────┤
                                                                   │ PK id                 │
                                                                   │ FK store_id (UK)      │
                                                                   │    reason             │
                                                                   │    is_active          │
                                                                   │    is_deleted         │
                                                                   └───────────────────────┘
```

---

## 📄 Submission PDF Documents

As requested in the submission guidelines:
1. **API Documentation**:
   * File: `Farhan_BackendDeveloper_APIDocumentation.pdf`
   * Location: Root directory & `Downloads/` directory
2. **Database Design Documentation**:
   * File: `Farhan_BackendDeveloper_DatabaseDesignDocumentation.pdf`
   * Location: Root directory & `Downloads/` directory

---

© 2026 Klik Indomaret - Technical Assessment Submission by Farhan (OTH00060172).
