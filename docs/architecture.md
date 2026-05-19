# Architecture Documentation

## Overview

The Advanced URL Shortener is designed as a scalable backend-focused system following layered architecture principles.

The system currently supports:

* URL shortening
* Custom aliases
* URL expiration
* Soft delete and restore
* Click tracking
* Scheduled cleanup

Future phases will introduce:

* Redis caching
* Bloom filters
* Analytics pipeline
* Rate limiting
* Authentication & authorization
* Kafka event streaming

---

# Current Architecture

```text id="d5oz80"
Client
   ↓
Controller Layer
   ↓
Service Layer
   ↓
Repository Layer
   ↓
PostgreSQL
```

---

# Layer Responsibilities

## Controller Layer

Responsible for:

* Receiving HTTP requests
* Request validation
* Returning API responses
* Redirect handling

## Service Layer

Responsible for:

* Business logic
* Alias validation
* Expiration checks
* Soft delete logic
* Click count updates

## Repository Layer

Responsible for:

* Database interaction
* JPA query execution
* Data persistence

---

# Current Request Flow

## URL Shortening Flow

```text id="1jlwmq"
Client Request
   ↓
Validate Request
   ↓
Generate Unique ID
   ↓
Generate Short Code
   ↓
Store in Database
   ↓
Return Short URL
```

---

# Redirect Flow

```text id="0spjlwm"
Client hits short URL
   ↓
Find mapping in database
   ↓
Check deleted status
   ↓
Check expiration
   ↓
Increment click count
   ↓
Redirect to original URL
```

---

# Future Architecture

## Planned Enhancements

### Redis Cache

Purpose:

* Faster URL lookups
* Reduced DB load

### Bloom Filter

Purpose:

* Fast alias existence checks
* Reduced database hits

### Kafka Event Streaming

Purpose:

* Async analytics processing
* Decoupled architecture

### Authentication System

Purpose:

* User-specific aliases
* Ownership management

---

# Scalability Goals

The architecture is being designed with future scalability in mind:

* Stateless application layer
* Distributed caching
* Async processing
* Horizontal scalability
* Event-driven architecture

