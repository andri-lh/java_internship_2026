# Event Booking Platform

Monorepo for the Event Booking Platform internship project.

## Repository layout

- `backend/` — single-module Spring Boot REST API, MySQL persistence, and backend tests.
- `frontend/` — React and TypeScript web client with a home page and public event browser.
- `compose.yaml` — starts the backend API and MySQL together.
- `.env.example` — local Docker Compose configuration template.

## Requirements

- Java 21
- Maven 3.9+
- Docker Desktop with Docker Compose

## Local setup

1. Copy `.env.example` to `.env` and replace the local placeholder secrets.
2. From the repository root, run:

```powershell
docker compose up --build
```

3. Stop with `docker compose down`. Add `-v` only when you intentionally want to delete the local database volume.

The API listens on port 8080.

## API documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

Public endpoints can be tried directly. For protected endpoints, register or log in, copy the `accessToken` from the login response, select **Authorize** in Swagger UI, and paste the token without the `Bearer ` prefix. The UI adds that prefix automatically. Access is still restricted by the account's role.

## Profiles

- `dev`: local development database settings; activated by Compose.
- `int`: integration-test settings. Testcontainers supplies the MySQL connection values during the test suite.
- `prod`: production settings; provide secrets and database connection values through the deployment environment.

Never commit `.env` or real credentials. In the `dev` profile, Hibernate `ddl-auto=update` creates and updates the local schema from the entity mappings. Avoid using `update` against production data; use a controlled schema process before production deployment.

## Tests

Run the fast Mockito unit tests without Docker:

```powershell
mvn -f backend/pom.xml '-Dtest=BookingServiceUnitTest,ReviewServiceUnitTest' test
```

Run the complete suite with Docker Desktop running:

```powershell
mvn -f backend/pom.xml clean test
```

The integration tests share one temporary MySQL container per Maven run. They clear test data before each case; no manually created database or Compose stack is needed for the tests. The `clean` goal removes stale files from `backend/target/` before running.

## Frontend development

In a second terminal, run `npm install` and `npm run dev` from `frontend/`. The Vite development server proxies `/api` requests to the backend on port 8080. See `frontend/README.md` for details.
