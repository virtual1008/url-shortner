# Architectural Decision: Redis Caching Layer & Performance Benchmark

## 1. Why Redis? (The Read-Intensive Problem)
A URL shortener like **LinkScale** is fundamentally a **read-intensive** application. In a real-world scenario, a single short link might be created once (one write) but clicked millions of times (millions of reads). 

Relying solely on a relational database like PostgreSQL for read operations introduces significant bottlenecks. Every redirection requires a network call, disk I/O, and consumes a database connection from the pool. Under high traffic, this leads to connection exhaustion and increased latency. We chose **Redis** as an in-memory data structure store to intercept these read requests, serving them in microseconds directly from RAM, thereby drastically reducing the load on the primary database.

## 2. Caching Strategy Implemented
To integrate Redis safely and efficiently, we implemented a **Cache-Aside (Lazy Loading)** strategy combined with **Dynamic TTL** and **Graceful Degradation**.

* **Cache-Aside:** When a user clicks a short link, the system queries Redis first. 
    * **Cache Hit:** The original URL is returned immediately.
    * **Cache Miss:** The system queries PostgreSQL, returns the URL to the user, and asynchronously updates Redis so the next request is a hit.
* **Dynamic Time-To-Live (TTL):** To prevent "cache staleness," Redis TTLs are dynamically calculated. The cache expiration is set to the *lesser* of our default retention policy (e.g., 24 hours) or the actual expiration timestamp of the specific URL. Redis automatically evicts the record the exact second the link expires.
* **Graceful Degradation:** The cache layer is wrapped in fault-tolerant `try-catch` blocks. If the Redis node crashes or experiences a network partition, the application swallows the exception and seamlessly falls back to PostgreSQL, ensuring the system stays online.

## 3. System Impact & Utility
By shifting the read burden to Redis, we achieved several critical system-wide impacts:
* **Database Protection:** PostgreSQL is shielded from traffic spikes (thundering herd problem), freeing up database CPU and connection pools for essential write operations (link creation, analytics tracking).
* **Microsecond Latency:** Bypassing disk reads allows redirections to happen almost instantly, vastly improving the end-user experience.
* **Cost Efficiency:** Scaling a Redis cluster to handle millions of reads is significantly cheaper and easier than vertically scaling a PostgreSQL cluster.

---

# LinkScale Performance Benchmark: Redis Caching Layer

## Objective
To evaluate the impact of introducing a Redis caching layer on the LinkScale URL redirection service, specifically measuring improvements in request throughput, tail latency, and system stability under concurrent load.

## Architecture Context
* **System:** LinkScale
* **Backend:** Spring Boot (Java 17)
* **Primary Database:** PostgreSQL
* **Cache:** Redis
* **Core Features Tested:** High-frequency URL redirection utilizing Snowflake IDs and Base62 encoding.
* **Load Testing Tool:** Apache JMeter

## Benchmark Results

| Metric | Without Redis (DB Only) | With Redis Cache | Improvement |
| :--- | :--- | :--- | :--- |
| **Total Requests** | 26,092 | 20,000 | - |
| **Throughput** | 17.7 req/sec | 1,162.0 req/sec | **~6,400% increase** |
| **Average Latency** | 164 ms | 67 ms | **~59% faster** |
| **Median Latency** | 17 ms | 9 ms | **~47% faster** |
| **90th Percentile** | 360 ms | 220 ms | **~38% faster** |
| **95th Percentile** | 728 ms | 359 ms | **~50% faster** |
| **99th Percentile** | 2,909 ms | 579 ms | **~80% faster** |
| **Max Response Time** | 8,864 ms | 934 ms | **~89% faster** |
| **Error Rate** | 0.763% | 0.000% | **100% elimination** |

## Key Engineering Observations

### 1. System Stability Under Load
Without Redis, LinkScale's primary database experienced heavy contention, resulting in a 0.763% error rate as connections queued or timed out. Introducing Redis eliminated database pressure during high-frequency reads, dropping the error rate to **0.000%**. Redis did not just improve speed; it fundamentally stabilized the system under concurrent traffic.

### 2. Tail Latency Reduction (99th Percentile)
At scale, tail latency dictates the user experience for the slowest requests. Falling back to the database resulted in 99th-percentile spikes of nearly 3 seconds (2,909 ms). The caching layer brought this down to **579 ms**. This ensures LinkScale provides a consistent, predictable redirection experience even during traffic spikes.

### 3. Throughput Explosion
The system jumped from 17.7 requests per second to 1,162 requests per second. 
> *Note on Methodology:* While Redis fundamentally increases throughput by serving from memory, a delta of this magnitude (64x) suggests that the underlying PostgreSQL connection pool may have been heavily bottlenecked, or the JMeter thread groups/ramp-up periods varied between tests. Future iterations of this benchmark will strictly isolate thread concurrency parity.

## Conclusion
The introduction of Redis caching successfully decoupled read-heavy redirection traffic from the primary PostgreSQL database. This architectural shift dramatically improved LinkScale's scalability, eliminated load-induced failure rates, and stabilized tail latency, proving essential for a high-frequency URL shortener.
