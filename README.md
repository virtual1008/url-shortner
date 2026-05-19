# LinkScale

## Scalable URL Infrastructure Platform

LinkScale is a backend-focused URL shortening platform built using Spring Boot and PostgreSQL with scalability, distributed systems, and production-grade backend engineering concepts in mind.

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

---

## Database & Persistence

* PostgreSQL integration
* Flyway database migrations
* Schema version management

---

# Tech Stack

| Technology      | Purpose                       |
| --------------- | ----------------------------- |
| Java 17         | Core language                 |
| Spring Boot     | Backend framework             |
| Spring Data JPA | ORM & persistence             |
| PostgreSQL      | Primary database              |
| Flyway          | Database migration management |
| Maven           | Dependency management         |
| Lombok          | Boilerplate reduction         |

---

# High-Level Architecture

```text id="1h99sa"
Client
   ↓
REST API Layer
   ↓
Service Layer
   ↓
Repository Layer
   ↓
PostgreSQL
```

---

# Core Engineering Concepts Implemented

* Distributed ID generation
* Base62 encoding
* Soft delete architecture
* Retention cleanup strategy
* Alias uniqueness validation
* Global exception handling
* Database migration versioning

---

# API Overview

## Create Short URL

```http id="m0r7iw"
POST /api/v1/url/shorten
```

Supports:

* Auto-generated short codes
* Custom aliases
* Expiration configuration

---

## Redirect To Original URL

```http id="plh79j"
GET /api/v1/url/{shortCode}
```

Features:

* Redirect handling
* Click count increment
* Expiration validation

---

## Soft Delete URL

```http id="jlwm44"
DELETE /api/v1/url/{shortCode}
```

---

## Restore Deleted URL

```http id="jlwm45"
POST /api/v1/url/restore/{shortCode}
```

---

# Example Request

## Create Short URL With Custom Alias

```json id="jlwm46"
{
  "originalUrl": "https://google.com",
  "customAlias": "google",
  "expiresInSeconds": 3600
}
```

---

# Example Response

```json id="jlwm47"
{
  "originalUrl": "https://google.com",
  "shortUrl": "http://localhost:8080/google",
  "shortCode": "google"
}
```

---

# Project Structure

```text id="jlwm48"
linkscale/
│
├── docs/
│   ├── architecture.md
│   ├── api-design.md
│   ├── database-design.md
│   ├── scaling.md
│   └── decisions/
│
├── src/
│
├── pom.xml
│
└── README.md
```

---

# Documentation

| File               | Purpose                       |
| ------------------ | ----------------------------- |
| architecture.md    | System architecture           |
| api-design.md      | API documentation             |
| database-design.md | Database schema & relations   |
| scaling.md         | Back-of-envelope calculations |
| decisions/         | Engineering decision records  |

---

# Running The Project Locally

## 1. Clone Repository

```bash id="jlwm49"
git clone <repository-url>
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

# Current Database Features

* URL mappings table
* Alias support
* Click tracking
* Expiration timestamps
* Soft delete timestamps

---

# Planned Features

## Caching Layer

* Redis integration
* Cache-aside strategy

---

## Bloom Filter

* Fast alias existence checks
* Reduced database reads

---

## Authentication & Authorization

* User registration/login
* User-owned aliases
* JWT authentication

---

## Analytics System

* Click analytics
* Geographic insights
* Device statistics

---

## Distributed System Enhancements

* Kafka event streaming
* Async analytics pipeline
* Horizontal scaling

---

# Engineering Focus Areas

This project focuses heavily on:

* Scalability
* Backend architecture
* Database design
* Performance optimization
* Production-ready engineering practices

---

# Future Scalability Vision

LinkScale is being designed to evolve toward:

* Distributed caching
* High read throughput
* Event-driven architecture
* Horizontal scalability
* Production-grade observability

---

# Author

Developed as a backend engineering and scalable systems learning project focused on real-world architecture practices.
