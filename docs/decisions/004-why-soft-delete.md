# Why Soft Delete Was Chosen

# Context

LinkScale is designed as a production-oriented URL infrastructure platform.

URLs in the system may need to be removed for multiple reasons:

* User deletion requests
* Expired links
* Abuse prevention
* Administrative cleanup
* Retention policy enforcement

A critical architectural decision was determining how deleted URLs should be handled inside the database.

---

# System Requirements

The deletion strategy should support:

* Safe URL deletion
* URL restoration capability
* Operational safety
* Auditability
* Future analytics support
* Scheduled cleanup processes
* Scalability

---

# Alternatives Considered

# Option 1: Hard Delete

Example:

```sql id="7w2e9p"
DELETE FROM url_mappings
WHERE short_code = 'abc123';
```

---

## Advantages

### Simple Implementation

The record is permanently removed from the database.

---

### Reduced Storage Usage

Deleted records no longer consume storage.

---

### Simpler Queries

No need to filter deleted records.

---

## Problems

### Permanent Data Loss

Once deleted:

* Recovery becomes impossible
* Mistakes cannot be reverted

---

### No Restore Capability

If a user accidentally deletes a URL:

```text id="9s1oqm"
Recovery is impossible.
```

---

### Poor Auditability

Hard delete removes historical information such as:

* Creation history
* Click analytics
* Ownership data
* Expiration history

---

### Risky Operational Behavior

Accidental deletes can permanently remove production data.

---

# Option 2: Archive Tables

Example:

```text id="e2cn7y"
Move deleted rows into another table
```

---

## Advantages

### Historical Preservation

Deleted data remains available.

---

### Smaller Main Table

Active data remains cleaner.

---

## Problems

### Increased Complexity

Requires:

* Data migration logic
* Multiple tables
* Additional query complexity

---

### Higher Maintenance Cost

Managing archive synchronization increases operational overhead.

---

### More Complex Restores

Restoring records becomes harder.

---

# Option 3: Soft Delete (Chosen)

LinkScale uses a soft delete strategy.

Instead of removing rows physically, records are marked as deleted.

---

# What Is Soft Delete

A soft delete marks a record as deleted using a timestamp.

Example:

```sql id="b1m3gk"
deleted_at TIMESTAMP
```

When deleting a URL:

```sql id="r4qk7n"
UPDATE url_mappings
SET deleted_at = NOW()
WHERE short_code = 'abc123';
```

The row still exists inside the database.

---

# Why Soft Delete Was Chosen

## Safe Data Removal

Soft delete prevents accidental permanent deletion.

The data remains recoverable.

---

## URL Restoration Support

Deleted URLs can later be restored.

Example flow:

```text id="9l3kde"
Delete URL → Restore URL
```

This supports better user experience.

---

## Better Operational Safety

Production systems should avoid destructive operations whenever possible.

Soft delete reduces operational risk.

---

## Future Analytics Support

Historical URL data remains available for:

* Click analysis
* Traffic analytics
* Audit logs
* Reporting systems

---

## Easier Debugging

Deleted records remain inspectable.

Useful for:

* Incident investigation
* Production debugging
* System monitoring

---

## Retention Policy Support

LinkScale introduces retention-based cleanup.

Flow:

```text id="p6d2wr"
Soft Delete → Retention Period → Permanent Cleanup
```

This creates safer lifecycle management.

---

# Why Retention-Based Cleanup Was Added

Immediately deleting records permanently can be dangerous.

Instead:

```text id="7x4vzp"
Deleted URLs are retained temporarily before permanent removal.
```

This allows:

* Recovery window
* Operational safety
* Better governance

---

# Current Cleanup Architecture

The system currently uses:

```text id="c9w2ms"
deleted_at column
+
Cleanup Scheduler
```

The scheduler periodically removes old deleted records after the retention period expires.

---

# Example Lifecycle

## URL Creation

```text id="8o3kfp"
URL created
```

---

## Soft Delete

```text id="7m1vqa"
deleted_at = current_timestamp
```

The URL becomes inaccessible.

---

## Restore (Optional)

```text id="x1b9dy"
deleted_at = null
```

The URL becomes active again.

---

## Final Cleanup

After retention expires:

```text id="o5l7qv"
Record permanently deleted by scheduler
```

---

# Query Design Impact

Active URLs are filtered using:

```sql id="z7j2ke"
WHERE deleted_at IS NULL
```

This ensures deleted URLs are excluded from normal operations.

---

# Tradeoff Summary

| Hard Delete      | Archive Tables     | Soft Delete (Chosen)    |
| ---------------- | ------------------ | ----------------------- |
| Simple           | Historical storage | Historical storage      |
| Permanent loss   | More complex       | Easy recovery           |
| No restore       | Restore possible   | Restore possible        |
| Risky operations | Extra maintenance  | Operational safety      |
| Minimal storage  | Multiple tables    | Single-table simplicity |

---

# Scalability Benefits

Soft delete architecture supports future features such as:

* User accounts
* Audit trails
* Analytics pipelines
* Admin dashboards
* Recovery systems
* Compliance policies

without redesigning deletion logic.

---

# Current Architectural Philosophy

The project prioritizes:

```text id="4f2zqe"
Operational safety with scalable lifecycle management.
```

Soft delete provides a practical balance between:

* Simplicity
* Recoverability
* Scalability
* Analytics support
* Operational reliability

---

# Final Decision

Soft delete was selected because it provides:

* Safe deletion behavior
* URL restoration capability
* Better operational safety
* Future analytics compatibility
* Retention-based cleanup support
* Easier debugging and monitoring

while maintaining clean architecture and production-oriented lifecycle management.

