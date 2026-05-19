# API Design Documentation

# Base URL

```text id="jlwmq1"
http://localhost:8080/api/v1/url
```

---

# Endpoints

## Create Short URL

### Endpoint

```http id="azq2k1"
POST /shorten
```

### Request Body

```json id="a4k7b9"
{
  "originalUrl": "https://google.com",
  "customAlias": "google",
  "expiresInSeconds": 3600
}
```

### Response

```json id="q9r2dx"
{
  "originalUrl": "https://google.com",
  "shortUrl": "http://localhost:8080/google",
  "shortCode": "google"
}
```

---

# Redirect URL

## Endpoint

```http id="t5l8wn"
GET /{shortCode}
```

### Description

Redirects user to original URL.

---

# Soft Delete URL

## Endpoint

```http id="6pxjzn"
DELETE /{shortCode}
```

### Description

Marks URL as deleted using soft delete strategy.

---

# Restore URL

## Endpoint

```http id="jlwm92"
POST /restore/{shortCode}
```

### Description

Restores previously deleted URL.

---

# Error Responses

## Alias Already Exists

```json id="n8c3lp"
{
  "timestamp": "2026-05-19T21:10:00",
  "status": 409,
  "error": "CONFLICT",
  "message": "Alias already exists"
}
```

---

# Validation Rules

## URL Validation

Accepted:

```text id="jlwmx8"
http://example.com
https://example.com
```

Rejected:

* invalid URLs
* blank URLs

---

# Future APIs

## Analytics API

```http id="jlwm33"
GET /analytics/{shortCode}
```

## User APIs

```http id="jlwm55"
POST /auth/register
POST /auth/login
```

## Rate Limit APIs

Planned internal monitoring APIs.

