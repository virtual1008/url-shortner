# API Design Documentation

# Base URL

```text
http://localhost:8080/api/v1/url
```

---

# API Overview

The URL Shortener service provides APIs for:

* Creating short URLs
* Supporting optional custom aliases
* Redirecting to original URLs
* Soft deleting URLs
* Restoring deleted URLs
* Tracking click counts

---

# 1. Create Short URL

## Endpoint

```http
POST /shorten
```

---

## Description

Creates a shortened URL.

The system supports:

* Auto-generated short codes using Snowflake ID + Base62 encoding
* Optional custom aliases provided by users

---

## Request Body

### Example With Custom Alias

```json
{
  "originalUrl": "https://google.com",
  "customAlias": "google",
  "expiresInSeconds": 3600
}
```

### Example Without Custom Alias

```json
{
  "originalUrl": "https://google.com",
  "expiresInSeconds": 3600
}
```

---

## Request Fields

| Field            | Type   | Required | Description             |
| ---------------- | ------ | -------- | ----------------------- |
| originalUrl      | String | Yes      | Original long URL       |
| customAlias      | String | No       | User-defined alias      |
| expiresInSeconds | Long   | No       | URL expiration duration |

---

## Success Response

```json
{
  "originalUrl": "https://google.com",
  "shortUrl": "http://localhost:8080/google",
  "shortCode": "google"
}
```

---

## Auto Generated Short Code Response

```json
{
  "originalUrl": "https://google.com",
  "shortUrl": "http://localhost:8080/bm4K8x",
  "shortCode": "bm4K8x"
}
```

---

# 2. Redirect To Original URL

## Endpoint

```http
GET /{shortCode}
```

---

## Description

Redirects the user to the original URL associated with the provided short code or alias.

Also performs:

* Expiration validation
* Soft delete validation
* Click count increment

---

## Example

```http
GET /google
```

or

```http
GET /bm4K8x
```

---

## Redirect Behavior

Returns:

```http
302 Found
```

with redirect to original URL.

---

# 3. Soft Delete URL

## Endpoint

```http
DELETE /{shortCode}
```

---

## Description

Marks a URL as deleted using soft delete strategy.

The record is not permanently removed immediately.

---

## Example

```http
DELETE /google
```

---

## Success Response

```json
"URL deleted successfully (soft delete)"
```

---

# 4. Restore URL

## Endpoint

```http
POST /restore/{shortCode}
```

---

## Description

Restores a previously soft deleted URL.

---

## Example

```http
POST /restore/google
```

---

## Success Response

```json
"URL restored successfully"
```

---

# Error Responses

---

## Alias Already Exists

### Status Code

```http
409 CONFLICT
```

### Response

```json
{
  "timestamp": "2026-05-19T21:10:00",
  "status": 409,
  "error": "CONFLICT",
  "message": "Alias already exists"
}
```

---

## URL Not Found

### Status Code

```http
404 NOT FOUND
```

### Response

```json
{
  "timestamp": "2026-05-19T21:10:00",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Short URL not found"
}
```

---

## URL Expired

### Status Code

```http
410 GONE
```

### Response

```json
{
  "timestamp": "2026-05-19T21:10:00",
  "status": 410,
  "error": "GONE",
  "message": "Short URL has expired"
}
```

---

# Validation Rules

## URL Validation

Accepted:

```text
http://example.com
https://example.com
```

Rejected:

* Invalid URLs
* Blank URLs
* Unsupported protocols

---

# Current Functional Features

* URL shortening
* Custom aliases
* Expiration support
* Click tracking
* Soft delete
* Restore URL
* Scheduled cleanup

---

# Future APIs

## Analytics API

```http
GET /analytics/{shortCode}
```

Purpose:

* View click statistics
* Traffic analytics

---

## Authentication APIs

```http
POST /auth/register
POST /auth/login
```

Purpose:

* User ownership
* Secure alias management

---

## Rate Limiting APIs

Planned for abuse prevention and monitoring.
