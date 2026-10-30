# rumi-notification-service

Alerts & Notification service of **Rumi**, the structural monitoring platform by Kuntur Labs.

| | |
|---|---|
| Bounded context | Alerts & Notification |
| Port | `8085` |
| Gateway routes | `/api/v1/alerts/**` (any valid token), `/api/v1/thresholds/**` (ADMINISTRATOR only) |
| Base package | `com.rumi.notification` |
| Database | `notificationsdb` (PostgreSQL) |

## Purpose

Raises and stores the alerts of a building:

- **Seismic risk alerts (US14, US16).** When Seismic Correlation calculates a risk index, the RabbitMQ
  listener calls `createAlertFromRisk`. The alert keeps the risk level as its severity. A `CRITICAL` risk
  sets `evacuationRequired = true` and tells the residents to evacuate (US16).
- **Threshold alerts (US15).** The administrator configures a warning and a critical value for each
  sensor. A reading at or above the warning value raises a `HIGH` alert; at or above the critical value, a
  `CRITICAL` alert. A sensor does not raise a new alert while it has one that is not resolved.

An alert moves from `OPEN` to `ACKNOWLEDGED` to `RESOLVED`.

Risk trend alerts (US27) and push delivery are planned for later sprints.

## Endpoints

| Verb | Path | Description | Request | Response | User story |
|---|---|---|---|---|---|
| GET | `/api/v1/alerts?buildingId={id}&status={status}` | List the alerts of a building, newest first; `status` is optional | none | `200` `AlertResponse[]` | US14, US15, US16 |
| GET | `/api/v1/alerts/{alertId}` | Get one alert with its instructions | none | `200` `AlertResponse` | US14, US16 |
| PATCH | `/api/v1/alerts/{alertId}/acknowledge` | Acknowledge an `OPEN` alert | none (`X-User-Id` from the gateway) | `200` `AlertResponse` | US14 |
| PATCH | `/api/v1/alerts/{alertId}/resolve` | Resolve an `OPEN` or `ACKNOWLEDGED` alert | none | `200` `AlertResponse` | US14, US15 |
| PUT | `/api/v1/thresholds/{sensorId}` | Create or change the threshold of a sensor | `ThresholdRequest` | `200` `ThresholdResponse` | US15 |
| GET | `/api/v1/thresholds/{sensorId}` | Get the threshold of a sensor | none | `200` `ThresholdResponse` | US15 |
| GET | `/api/v1/thresholds?buildingId={id}` | List the thresholds of a building | none | `200` `ThresholdResponse[]` | US15 |

Implemented functional endpoints: 7.

Errors follow RFC 7807 (`application/problem+json`): `400` invalid input, `403` a resident asked for
another building, `404` unknown alert or threshold, `409` the alert is not in a state that allows the change.

Only with the `dev` profile, two more endpoints raise alerts by hand while the messaging listeners are
not running. They are not counted as functional endpoints:

| Verb | Path | Description |
|---|---|---|
| POST | `/api/v1/alerts/simulations/risk` | Simulate a risk index (`201` with the alert) |
| POST | `/api/v1/alerts/simulations/readings` | Simulate a sensor reading (`201` with the alert, or `204` if no alert is raised) |

The skeleton endpoint `GET /api/v1/alerts/health` is still available.

### Access rules

The gateway validates the JWT and forwards `X-User-Id`, `X-Role` and `X-Building-Id` (comma-separated).
This service adds one rule: a `RESIDENT` only sees the alerts of the buildings in their token (`403`
otherwise). Requests without these headers did not go through the gateway and are not restricted.

### Example

```json
{
  "id": "6f1d2c3b-4a5e-4f6d-8c7b-9a0e1f2d3c4b",
  "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
  "type": "SEISMIC_RISK",
  "severity": "CRITICAL",
  "status": "OPEN",
  "title": "Seismic risk CRITICAL",
  "message": "Critical structural risk after the seismic event. Evacuate the building now using the nearest evacuation route.",
  "evacuationRequired": true,
  "riskIndexId": "4d2b8f6e-1a3c-4e5d-9f7b-2c4a6e8d0f13",
  "seismicEventId": "9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b",
  "sensorId": null,
  "zone": null,
  "measuredValue": null,
  "occurredAt": "2026-10-30T14:32:00Z",
  "createdAt": "2026-10-30T14:32:04Z",
  "acknowledgedAt": null,
  "acknowledgedBy": null,
  "resolvedAt": null
}
```

## Contract for the risk event listener

The RabbitMQ listener of the risk event calls one method of the application layer:

```java
Alert alertService.createAlertFromRisk(new CreateAlertFromRiskCommand(
        buildingId,      // UUID, required
        riskIndexId,     // UUID, required: identifies the risk index
        seismicEventId,  // UUID, required
        riskLevel,       // "LOW", "MEDIUM", "HIGH" or "CRITICAL"
        calculatedAt));  // Instant, required
```

- The method is **idempotent by `riskIndexId`**. If the broker redelivers the message, the alert already raised is
  returned and no duplicate is created.
- An unknown risk level throws `IllegalArgumentException` and nothing is saved. The listener should reject
  the message without requeueing it.

Sensor readings follow the same pattern with `evaluateReading(EvaluateReadingCommand)`.

## API documentation

- Swagger UI: <http://localhost:8085/swagger-ui.html>
- OpenAPI spec: <http://localhost:8085/v3/api-docs>
- Exported spec: [`docs/openapi.json`](docs/openapi.json)

To export the spec again, start the service with the `dev` profile and run:

```sh
curl -s http://localhost:8085/v3/api-docs | python3 -m json.tool > docs/openapi.json
```

## Run

The repository includes the Maven wrapper, so only JDK 21 is required. Use `mvnw.cmd` on Windows.

### Development profile (no external infrastructure)

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile uses an in-memory H2 database. It loads sample data with the fixed identifiers shared
by the other Rumi services:

| Resource | Id | Data |
|---|---|---|
| Building | `7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d` | Torre Miraflores |
| Sensor threshold | `5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11` | warning `0.05`, critical `0.10` |
| Alert | `0b7e4c2a-9d1f-4e3b-8a6c-5f2d1e0c9b8a` | `SEISMIC_RISK`, `MEDIUM`, `OPEN` |

```sh
curl "http://localhost:8085/api/v1/alerts?buildingId=7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d"
```

### Default profile (PostgreSQL)

Create the database and its tables with [`docs/schema.sql`](docs/schema.sql). Hibernate runs with
`ddl-auto=validate`, so the tables must exist.

```sh
./mvnw spring-boot:run
```

| Variable | Default |
|---|---|
| `SERVER_PORT` | `8085` |
| `NOTIFICATION_DB_URL` | `jdbc:postgresql://localhost:5432/notificationsdb` |
| `NOTIFICATION_DB_USERNAME` | `postgres` |
| `NOTIFICATION_DB_PASSWORD` | `postgres` |

With Docker Compose (`rumi-backend/deploy`), the `postgres` container creates `notificationsdb` and its
tables on first start.

## Test

```sh
./mvnw test
```

- Unit tests (domain, application, web, persistence on in-memory H2) need no infrastructure.
- `NotificationServiceApplicationTest` starts the service with the `dev` profile and runs the whole flow.
- The Cucumber suite (`src/test/resources/com/rumi/notification/alerts.feature`) runs against a real
  PostgreSQL started with Testcontainers, so **Docker must be running**. The tables come from
  `docs/schema.sql` and Hibernate validates them.

## Structure

```
com.rumi.notification
├── application                  use cases (AlertApplicationService) and their commands
├── domain
│   ├── model                    Alert, SensorThreshold and their enums
│   └── repository               repository ports
└── infrastructure
    ├── config                   Clock
    ├── persistence              JPA entities and repository adapters
    ├── seed                     sample data of the dev profile
    └── web                      controllers, DTOs, error handling and OpenAPI examples
```
