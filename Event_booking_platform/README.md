# Event Booking Platform

Single-module Spring Boot backend for the internship EventBooking assignment. The application is being implemented requirement by requirement.

## Requirements

- Java 21
- Maven 3.9+ (or use the Maven wrapper when one is added)
- Docker Desktop with Docker Compose

## Local setup

1. Copy `.env.example` to `.env` and replace the local placeholder secrets.
2. Start the API and MySQL with `docker compose up --build`.
3. Stop with `docker compose down`. Add `-v` only when you intentionally want to delete the local database volume.

The API listens on port 8080. OpenAPI UI is available at `/swagger-ui.html`.

## Profiles

- `dev`: local development database settings; activated by Compose.
- `int`: integration-test settings. Testcontainers supplies the MySQL connection values during the test suite.
- `prod`: production settings; provide secrets and database connection values through the deployment environment.

Never commit `.env` or real credentials. In the `dev` profile, Hibernate `ddl-auto=update` creates and updates the local schema from the entity mappings. Avoid using `update` against production data; use a controlled schema process before production deployment.

## Tests

Run the fast Mockito unit tests without Docker:

```powershell
mvn '-Dtest=BookingServiceUnitTest,ReviewServiceUnitTest' test
```

Run the complete suite with Docker Desktop running:

```powershell
mvn clean test
```

The integration tests share one temporary MySQL container per Maven run. They clear test data before each case; no manually created database or Compose stack is needed for the tests. The `clean` goal removes stale files from `target/` before running.

## Initial structure

- `config`: OpenAPI and application configuration
- `controller`: REST resource boundaries
- `dto`: request and response shapes
- `entity`: JPA domain model and enums
- `exception`: API error handling boundary
- `mapper`: DTO/entity mapping boundary
- `repository`: Spring Data persistence interfaces
- `security`: JWT and authorization boundaries
- `service`: application service interfaces and implementation classes
- `src/test`: integration and unit test locations
