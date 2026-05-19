# Database Design Documentation

# Database

PostgreSQL

---

# Current Tables

## url_mappings

Stores URL shortening data and metadata.

---

# Table Schema

| Column Name  | Type         | Description                   |
| ------------ | ------------ | ----------------------------- |
| id           | BIGINT       | Unique distributed identifier |
| short_code   | VARCHAR(20)  | Unique short URL identifier   |
| original_url | TEXT         | Original long URL             |
| created_at   | TIMESTAMP    | URL creation time             |
| expires_at   | TIMESTAMP    | URL expiration time           |
| deleted_at   | TIMESTAMP    | Soft delete timestamp         |
| click_count  | BIGINT       | Total redirect count          |
| alias        | VARCHAR(255) | Custom alias                  |

---

# Constraints

## Primary Key

```sql id="upjlwm"
PRIMARY KEY(id)
```

## Unique Constraint

```sql id="qpm0eo"
UNIQUE(short_code)
```

Purpose:

* Prevent duplicate aliases
* Ensure URL uniqueness

---

# Soft Delete Strategy

Instead of permanently deleting records:

```text id="jlwm0q"
deleted_at != NULL
```

is used to mark records as deleted.

Benefits:

* Recovery support
* Audit capability
* Safer deletion strategy

---

# Retention Policy

Deleted URLs are cleaned after retention period using scheduled cleanup jobs.

Purpose:

* Reduce storage usage
* Maintain recoverability window

---

# Click Tracking

Each redirect increments:

```text id="t7s8hf"
click_count
```

Purpose:

* Analytics
* Popularity tracking
* Future dashboard support

---

# Future Database Enhancements

## users table

Purpose:

* Authentication
* Ownership management

## url_analytics table

Purpose:

* Detailed click analytics
* IP tracking
* Device tracking

## rate_limit table

Purpose:

* Abuse prevention
* Request throttling

---

# Future Optimizations

## Indexing

Planned indexes:

```sql id="7l3p8g"
INDEX(short_code)
INDEX(alias)
INDEX(created_by, alias)
```

## Read Replicas

Future scaling consideration for high read traffic.

## Partitioning

Potential future partitioning for analytics tables.

