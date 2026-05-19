
# Why SQL Was Chosen Instead of NoSQL

# Context

LinkScale is a scalable URL shortening platform focused on backend engineering, system design, and production-grade architecture practices.

One of the early architectural decisions was selecting the primary database type for storing URL mappings and related metadata.

The main options considered were:

* Relational Database (SQL)
* NoSQL Database

The current implementation uses PostgreSQL as the primary datastore.

---

# System Requirements

The URL shortening system currently requires:

* Unique short code generation
* Strong consistency
* Alias uniqueness validation
* Transactional updates
* Click count updates
* Soft delete support
* Expiration handling
* Reliable cleanup operations

Future planned features include:

* Authentication
* User-owned aliases
* Analytics
* Event pipelines
* Distributed caching

---

# Alternatives Considered

# Option 1: NoSQL Database

Examples:

* MongoDB
* Cassandra
* DynamoDB

---

## Advantages

### High Horizontal Scalability

NoSQL systems scale horizontally very efficiently.

Useful for:

* Massive write throughput
* Distributed workloads
* Multi-region deployments

---

### Flexible Schema

Easy to evolve document structure without strict schema migrations.

---

### High Availability

Many NoSQL systems are optimized for distributed availability.

---

## Problems For Current Use Case

### Alias Uniqueness Complexity

The system requires globally unique aliases.

Example:

```text id="jlwm54"
/google
/facebook
/myportfolio
```

In many NoSQL systems, enforcing strong uniqueness constraints becomes more complex compared to SQL databases.

---

### Strong Consistency Requirements

URL redirects require accurate mappings.

Incorrect redirects due to eventual consistency could create:

* Invalid routing
* Broken links
* Duplicate aliases

Strong consistency is more naturally handled in relational databases.

---

### Transactional Operations

Current and future operations involve transactional behavior:

* URL creation
* Alias validation
* Soft delete
* Restore operations
* Analytics updates

SQL databases provide mature ACID guarantees.

---

### Relational Future Features

Future planned features introduce relational behavior:

* Users
* Ownership
* Role management
* Analytics relationships

These are naturally modeled using relational databases.

---

# Option 2: SQL Database (Chosen)

Chosen database:

* PostgreSQL

---

# Why PostgreSQL Was Selected

## Strong Consistency

The system requires correctness over eventual consistency.

Examples:

* Alias uniqueness
* Accurate redirect mapping
* Reliable restore/delete operations

PostgreSQL provides strong consistency guarantees.

---

## Native Uniqueness Constraints

Example:

```sql id="jlwm55"
UNIQUE(short_code)
```

This allows:

* Reliable alias validation
* Simpler application logic
* Safer concurrent writes

---

## Excellent Transaction Support

PostgreSQL provides mature ACID transaction handling.

Useful for:

* URL creation
* Cleanup jobs
* Click tracking
* Future analytics pipelines

---

## Better Query Capabilities

Future analytics may require:

* Filtering
* Aggregations
* Reporting
* Joins

SQL databases are significantly stronger for analytical querying.

---

## Mature Ecosystem

PostgreSQL has:

* Excellent tooling
* Strong indexing support
* Reliable migrations
* Observability support
* Production stability

---

## Easier Early-Stage Development

At the current project scale:

* SQL complexity is lower
* Development speed is faster
* Operational overhead is smaller

---

# Why NoSQL May Still Be Introduced Later

Although PostgreSQL is the primary datastore, NoSQL technologies may still be introduced for specific workloads.

Examples:

| Technology    | Purpose                      |
| ------------- | ---------------------------- |
| Redis         | Caching                      |
| Cassandra     | High-volume analytics events |
| Elasticsearch | Search & analytics           |
| Kafka         | Event streaming              |

---

# Current Architectural Philosophy

The system follows:

```text id="jlwm56"
Use the right database for the right problem.
```

Currently:

* Relational consistency is more important than massive distributed scale.

Future architecture may evolve into:

* Polyglot persistence
* Event-driven systems
* Distributed caching layers

---

# Tradeoff Summary

| SQL (Chosen)                  | NoSQL                            |
| ----------------------------- | -------------------------------- |
| Strong consistency            | Eventual consistency common      |
| Native uniqueness constraints | More complex uniqueness handling |
| Better relational modeling    | Better horizontal scaling        |
| ACID transactions             | Distributed scalability          |
| Strong analytics support      | Flexible schema                  |

---

# Final Decision

PostgreSQL was selected because the current system requirements prioritize:

* Data correctness
* Consistency
* Transactional safety
* Alias uniqueness
* Simpler operational complexity

The architecture remains flexible enough to later integrate NoSQL systems where they provide clear advantages.
