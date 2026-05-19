# Advanced URL Shortener

A scalable backend-focused URL shortening service built using Spring Boot, PostgreSQL, Redis, and Flyway.

## Features

* URL shortening
* Custom aliases
* URL expiration
* Soft delete and restore
* Click tracking
* Scheduled cleanup
* Flyway migrations
* Redis caching (upcoming)
* Bloom filter optimization (upcoming)

## Tech Stack

* Java 17
* Spring Boot
* PostgreSQL
* Redis
* Flyway
* Docker (upcoming)

## Architecture

The application follows a layered architecture:

Controller → Service → Repository → Database

## API Endpoints

### Create Short URL

POST /api/v1/url/shorten

### Redirect URL

GET /api/v1/url/{shortCode}

### Delete URL

DELETE /api/v1/url/{shortCode}

### Restore URL

POST /api/v1/url/restore/{shortCode}

## Running Locally

1. Clone repository
2. Configure PostgreSQL
3. Run Flyway migrations
4. Start Redis
5. Run Spring Boot application

## Future Enhancements

* Redis caching
* Bloom filter
* Kafka analytics pipeline
* Authentication system
* Rate limiting
* Distributed deployment
