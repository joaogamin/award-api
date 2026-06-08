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


## Architecture

The persistence layer uses a normalised 3NF model (`movie`, `producer`, `movie_producer`) created entirely via `schema.sql` — Hibernate runs with `ddl-auto=none` and does not alter the schema.

Interval calculation is delegated to a SQL `VIEW` (`vw_producer_intervals`) using the `LEAD()` window function partitioned by producer. The Java service only executes two queries (`MIN` / `MAX`) and maps the results — no loop-based interval arithmetic in application code.

CSV data is loaded at startup in batches of 50 via `@EventListener(ApplicationReadyEvent.class)`, which fires after `schema.sql` has run.