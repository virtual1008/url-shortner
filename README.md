# LinkScale

## Scalable URL Infrastructure Platform

LinkScale is a highly scalable, event-driven URL shortening platform built with Spring Boot. Moving beyond basic CRUD operations, this project is engineered to handle massive scale, demonstrating production-grade backend patterns, asynchronous processing, and high-throughput data pipelines within a modular monolith architecture.

The project is being developed phase-by-phase to simulate how real-world large-scale systems are designed and evolved over time.

---

# Project Goals

The primary goal of LinkScale is not only to shorten URLs, but also to demonstrate:

* Distributed system design principles
* Backend scalability techniques
* Production-grade architecture patterns
* Engineering decision making
* Performance optimization strategies

---

## 🚀 High-Level Architecture

LinkScale utilizes an event-driven architecture to decouple fast URL redirection from heavy analytical aggregations, ensuring that click-tracking never slows down the user experience.
```text
[Client] 
   │
   ▼ (Rate Limited)
[Spring Boot REST API]
   │
   ├──► [Bloom Filter] (In-memory existence check)
   │
   ├──► [Redis Cache] (Sub-millisecond read layer)
   │
   ▼
[PostgreSQL Database] (URL Mappings)
   │
   ▼ (Async Fire-and-Forget)
[Apache Kafka Topic: click-events]
   │
   ▼
[Analytics Batch Worker] (Thread-safe Concurrent Queue)
   │
   ▼
[PostgreSQL Database] (URL Clicks & Time-Series Aggregation)
```

# 🧠 Core Engineering Achievements
This project focuses heavily on solving traditional bottleneck issues found in high-traffic applications:
* **Distributed ID Generation:** Replaced standard database auto-increment IDs with a custom Twitter Snowflake Algorithm, allowing the application to generate unique, collision-free 64-bit IDs in-memory without database locking.

* **Thundering Herd Protection:** Implemented a Bloom Filter to instantly reject invalid or malicious URL requests in-memory, protecting the database from unnecessary read queries.

* **High-Performance Reads:** Integrated a Redis Cache-Aside strategy to serve frequently accessed ("hot") links in milliseconds.

* **Event-Driven Analytics:** Decoupled click-tracking from the redirection critical path using Apache Kafka. Click telemetry is ingested asynchronously and processed in batches by background workers.

* **API Protection:** Implemented Token Bucket Rate Limiting to prevent API abuse, brute-force attacks, and DDoS attempts.

* **Automated Data Lifecycle:** Engineered a cron-based scheduler to automatically purge soft-deleted URLs after a configurable 30-day retention period.



# Current Features

## URL Management

* URL shortening
* Auto-generated short codes
* Optional custom aliases
* URL expiration support

---

## URL Lifecycle Management

* Soft delete support
* Restore deleted URLs
* Retention-based cleanup scheduler

---

## Analytics

* Click count tracking
* Redirect statistics foundation
* Time-series daily/hourly aggregation
* Geographic and device/browser breakdowns

---

## Database & Persistence

* PostgreSQL integration
* Flyway database migrations
* Schema version management

---

## High-Performance & Caching

* Redis Cache-Aside implementation
* Sub-millisecond read latency for frequently accessed ("hot") links
* In-memory Bloom Filter for instant invalid/malicious URL rejection

---

## Resilience & Security

* Distributed Token Bucket Rate Limiting (backed by Redis)
* API abuse, brute-force, and DDoS protection
* Global exception handling and strict API validation

---

## Event-Driven Processing

* Apache Kafka integration for decoupled architecture
* Asynchronous click-event ingestion (zero impact on redirect latency)
* Thread-safe concurrent queues and background batch workers

---

## Distributed Systems Infrastructure

* Custom Twitter Snowflake Algorithm for ID generation
* Collision-free, database-lock-free 64-bit ID creation
* Base62 encoding for maximum URL compression
---

# 🛠️ Tech Stack

| Domain          | Technology                         | Purpose                                                         |
| --------------- | ---------------------------------- | --------------------------------------------------------------- |
| Core            | Java 17, Spring Boot 3             | Primary backend framework                                       |
| Data Layer      | PostgreSQL, Spring Data JPA        | Relational persistence & aggregation                            |
| Infrastructure  | Redis, Apache Kafka                | Distributed caching & Message Broker                            |
| Algorithms      | Snowflake, Bloom Filter, Base62    | Distributed IDs, Probabilistic filtering, Encoding              |
| Resilience      | Redis Rate Limiter                 | In-memory API Rate Limiting                                     |
| API & Docs      | SpringDoc OpenAPI (Swagger)        | Interactive API contract and documentation                      |
| Testing         | JUnit 5, Mockito                   | Comprehensive unit and integration test suite                   |
| Performance     | K6                                 | High-concurrency load testing                                   |
| Migrations      | Flyway                             | Database schema versioning                                      |
---


## 🧠 Core Engineering Concepts Implemented

* **Distributed ID Generation:** Implemented a custom Twitter Snowflake algorithm to generate 64-bit, collision-free identifiers in-memory, bypassing database locking and enabling horizontal scalability.
* **Base62 Encoding:** Utilized Base62 conversion to compress 64-bit numerical IDs into ultra-compact, URL-safe alphanumeric strings.
* **Soft Delete Architecture:** Engineered a safe-delete mechanism using boolean flags and timestamps, ensuring analytical data remains intact while hiding the URL from public routing.
* **Retention Cleanup Scheduler:** Built an automated, background cron job that safely purges soft-deleted records from the database once their 30-day retention period expires.
* **Alias Uniqueness Validation:** Implemented thread-safe, strict validation layers to ensure custom user-defined short links never collide with auto-generated hashes.
* **Standardized Exception Handling (RFC 7807):** Centralized API error management using `@ControllerAdvice` to ensure all client errors are returned in a predictable, strictly formatted JSON payload adhering to the RFC 7807 specification (Problem Details for HTTP APIs).
* **Database Migration Versioning:** Integrated Flyway to treat database schemas as code, ensuring all table creations and alterations are version-controlled and deployed automatically on startup.

---

# 🔌 API Reference
Base URL: ```http://localhost:8080``` (Also available via Swagger UI at ```/swagger-ui/index.html```)
## 🔗 URL Management (```url-shortener-controller```)

|Method|Endpoint|Description|
|-------|--------|-----------|
|POST|/api/v1/url/shorten|Creates a new short URL (supports custom aliases). |
|GET|/api/v1/url/{shortCode}|Redirects to the original URL and triggers async click event.|
|GET|/api/v1/url|Retrieves a paginated list of all active URLs.|
|DELETE|/api/v1/url/{shortCode}|Soft-deletes a specific URL (moves to trash).|
|PUT|/api/v1/url/restore/{shortCode}|Restores a previously soft-deleted URL.|
|GET|/api/v1/url/trash|Retrieves a paginated list of all soft-deleted URLs pending cleanup.|



## 📊 Analytics Engine (```analytics-controller```)
|Method|Endpoint|Description|
|---------|-----------|-------|
|GET|/api/v1/analytics/{shortCode}|Retrieves complete analytics data for a specific short code.|
|GET|/api/v1/analytics/{shortCode}/summary|"Retrieves high-level KPI summary (e.g., total clicks)."|
|GET|/api/v1/analytics/{shortCode}/timeseries|Retrieves daily/hourly click aggregation for charts.|
|GET|/api/v1/analytics/{shortCode}/devices|Retrieves click breakdown by Operating System and Browser.|
|GET|/api/v1/analytics/{shortCode}/locations|Retrieves geographic click distribution.|


# 🧪 Testing & Quality Assurance
Quality and reliability are enforced using a strict testing pyramid:

**1.Unit Testing:** Isolated business logic validation (e.g., Snowflake ID generation, cron math) using JUnit 5 and Mockito.

**2.Integration Testing:** Full Spring Context loads verifying database constraints, ```Redis TTL``` expiration, and ```API layer routing```.

**3.End-to-End Testing:** Simulating full user lifecycles ```(Shorten -> Cache -> Redirect -> Analytics Queue)```.

**4.Global Test Configuration:** Segregated ```application-test.yml``` ensuring tests run against ephemeral databases without corrupting local data.

# Running The Project Locally

## 1. Clone Repository

```bash id="jlwm49"
git clone https://github.com/virtual1008/url-shortner.git
cd url-shortner
```

---

## 2. Configure PostgreSQL

Update database credentials in:

```properties id="jlwm50"
src/main/resources/application.properties
```

---

## 3. Run Flyway Migrations

Flyway migrations execute automatically during application startup.

---

## 4. Start Application

```bash id="jlwm51"
mvn spring-boot:run
```

---

## 🗺️ Future Scalability & Feature Vision

LinkScale is continually evolving to simulate a growing enterprise infrastructure. The next phases of development are focused on distributed orchestration and advanced user management:

* **Microservice Architecture Transition:** Decomposing the current event-driven modular monolith into distinct, independently deployable microservices (API Gateway, Core URL Service, Analytics Service).
* **Cloud-Native Orchestration (K8s):** Containerizing the application ecosystem and deploying to a Kubernetes cluster for auto-scaling, self-healing, and native load balancing.
* **Distributed Observability:** Implementing OpenTelemetry, Prometheus, and Grafana to achieve complete cross-service tracing and centralized logging across the microservice network.
* **Automated CI/CD Pipelines:** Establishing robust GitHub Actions workflows for automated test execution, Docker image generation, and seamless registry deployment.
* **Identity & Access Management (IAM):** Integrating JWT-based authentication and Role-Based Access Control (RBAC) to support secure user accounts and strict link ownership.
* **Advanced Link Access Controls:** Expanding the domain model to support visibility toggles, allowing users to designate shortened URLs as Public, Private, or securely Shared.

---

# Author

Developed by Ujjval Tailor

Backend Engineer focused on:

* Scalable systems
* Distributed architecture
* System design
* Backend infrastructure engineering

LinkedIn: https://www.linkedin.com/in/ujjval-tailor-318850198/

