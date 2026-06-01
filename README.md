# Award API

REST API that processes Golden Raspberry Awards data to find producers with the shortest and longest intervals between consecutive wins.

## Requirements

- JDK 8+
- No external database or services required

## Running the application

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

The application starts on port **8080** by default and seeds the database from `movielist.csv` at startup.

```bash
./mvnw package
java -jar target/award-api-*.jar
```

## Running the tests

```bash
./mvnw test
```

The test suite boots the full Spring context against a random port and validates the HTTP contract and computed interval values against the real CSV data.

## API

### `GET /api/awards/intervals`

Returns the producers with the minimum and maximum intervals between consecutive wins. All producers that share a boundary interval are included — ties are never dropped.

**Response: `200 OK`**

```json
{
  "min": [
    {
      "producer": "Joel Silver",
      "interval": 1,
      "previousWin": 1990,
      "followingWin": 1991
    }
  ],
  "max": [
    {
      "producer": "Matthew Vaughn",
      "interval": 13,
      "previousWin": 2002,
      "followingWin": 2015
    }
  ]
}
```

## H2 Console

The embedded H2 console is available at `http://localhost:8080/h2-console` while the application is running.

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:awarddb` |
| Username | `sa` |
| Password | *(empty)* |

## Built with

| | Technology | Version |
|---|---|---|
| Language | Java | 1.8 |
| Framework | Spring Boot | 2.7.18 |
| Persistence | Spring Data JPA + Hibernate | — |
| Database | H2 (in-memory) | — |
| Build | Maven Wrapper | — |
| Testing | JUnit 5, Spring Boot Test | — |


## Future Improvements (Architectural Vision)

While the current architecture (in-heap parsing and H2 in-memory DB) perfectly satisfies the requirements of a fast, local, and contained execution for this test, a production-ready system dealing with daily file integrations (especially in critical contexts) would benefit from the following evolution:

*   **Batch Processing:** Transitioning from synchronous inline parsing to a robust chunk-based processing model (e.g., using Spring Batch) to handle massive CSV files efficiently without memory exhaustion.
*   **Idempotency & Upserts:** Implementing database constraints and upsert logic to ensure that processing the same file multiple times does not result in duplicated records (handling full vs. delta daily loads).
*   **Event-Driven Ingestion:** If the file is submitted via a REST endpoint or an external source, the processing should be decoupled using messaging queues (e.g., Kafka, RabbitMQ, or JMS) to process the data asynchronously and return immediate feedback to the client.
*   **Fault Tolerance (Skip Policy):** Implementing strict skip-policies for malformed CSV lines, directing them to a Dead Letter Table/Queue for business review, rather than rolling back or failing the entire batch integration.
*   **Observability:** Adding externalized metrics and logging to monitor processing duration, total records ingested, and error rates during the daily pipeline.
