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
  sets `evacuationRequired = true`, which tells the apps to show the building's evacuation plan (US16).
- **Threshold alerts (US15).** The administrator configures a warning and a critical value for each
  sensor. A reading at or above the warning value raises a `HIGH` alert; at or above the critical value, a
  `CRITICAL` alert. A sensor does not raise a new alert while it has one that is not resolved.

An alert moves from `OPEN` to `ACKNOWLEDGED` to `RESOLVED`.

### Alert wording

Rumi **does not order evacuations** from risk levels or thresholds. Every message, in Spanish for the
residents, reports an *anomaly* with its source and when it was detected, and refers them to the building's
plan and to the authorities. Rumi complements SASPe; it is not an official alert.

| Alert | Message (example) |
|---|---|
| Seismic `CRITICAL` / `HIGH` | Anomalía estructural crítica detectada tras un sismo. Fuente: reporte del IGP (evento `<id>`) y sensores del edificio. Detectado: 30/10/2026 09:32 (hora de Lima). Siga el plan de evacuación de su edificio y las indicaciones de las autoridades. Rumi complementa al SASPe; no es una alerta oficial. |
| Seismic `MEDIUM` | … Se recomienda inspeccionar las zonas afectadas. Siga el plan de su edificio y las indicaciones de las autoridades. |
| Seismic `LOW` | … Manténgase atento a las indicaciones de las autoridades. |
| Threshold | Anomalía detectada por el sensor `<id>` en la zona FLOOR-3-NORTH: lectura 0,120 sobre el umbral crítico 0,100. Detectado: … (hora de Lima). Siga el plan de su edificio y las indicaciones de las autoridades. |

"Detectado" is when Rumi analysed the risk or recorded the reading, not when the earthquake happened.
`evacuationRequired` keeps its name and column (no migration): it is `true` for a `CRITICAL` seismic risk
and means "show the building's evacuation plan". Alerts stored before this change keep their old text.

Risk trend alerts (US27) and push delivery are planned for later sprints.

## Endpoints

| Verb | Path | Description | Who | Request | Response | User story |
|---|---|---|---|---|---|---|
| GET | `/api/v1/alerts?buildingId={id}&status={status}` | List the alerts of a building, newest first; `status` is optional | administrator or resident of the building | none | `200` `AlertResponse[]` | US14, US15, US16 |
| GET | `/api/v1/alerts/{alertId}` | Get one alert with its instructions | administrator or resident of the building | none | `200` `AlertResponse` | US14, US16 |
| PATCH | `/api/v1/alerts/{alertId}/acknowledge` | Acknowledge an `OPEN` alert | administrator of the building | none (`X-User-Id` from the gateway) | `200` `AlertResponse` | US14 |
| PATCH | `/api/v1/alerts/{alertId}/resolve` | Resolve an `OPEN` or `ACKNOWLEDGED` alert | administrator of the building | none | `200` `AlertResponse` | US14, US15 |
| PUT | `/api/v1/thresholds/{sensorId}` | Create or change the threshold of a sensor | administrator of the building | `ThresholdRequest` | `200` `ThresholdResponse` | US15 |
| GET | `/api/v1/thresholds/{sensorId}` | Get the threshold of a sensor | administrator of the building | none | `200` `ThresholdResponse` | US15 |
| GET | `/api/v1/thresholds?buildingId={id}` | List the thresholds of a building | administrator of the building | none | `200` `ThresholdResponse[]` | US15 |

Implemented functional endpoints: 7.

Errors follow RFC 7807 (`application/problem+json`): `400` invalid input, `401` no user identity from the
gateway, `403` the building is not in the token or the action is for its administrator, `404` unknown alert
or threshold, `409` the alert is not in a state that allows the change.

Only with the `dev` profile, two more endpoints raise alerts by hand while the messaging listeners are
not running. They are not counted as functional endpoints:

| Verb | Path | Description |
|---|---|---|
| POST | `/api/v1/alerts/simulations/risk` | Simulate a risk index (`201` with the alert) |
| POST | `/api/v1/alerts/simulations/readings` | Simulate a sensor reading (`201` with the alert, or `204` if no alert is raised) |

The skeleton endpoint `GET /api/v1/alerts/health` is still available.

### Access rules

The gateway validates the JWT and forwards `X-User-Id`, `X-Role` and `X-Building-Id` (comma-separated),
after removing any of these headers sent by the client. This service checks building membership for every
role, as defense in depth:

- A request without `X-User-Id` or `X-Role` did not go through the gateway and gets `401`.
- Only `ADMINISTRATOR` and `RESIDENT` reach a building, and only the buildings of their token (`403`
  otherwise). Any other role gets `403`; platform roles do not exist yet.
- Acknowledging or resolving an alert and managing thresholds are for the `ADMINISTRATOR` of the building;
  a `RESIDENT` gets `403`.

The `buildingIds` claim comes from IAM. Linking users to their buildings in IAM is still pending: until it
is done, the tokens carry an empty list and every building request through the gateway gets `403`.

To call the service directly (Swagger UI, Postman), send the three headers yourself, for example
`X-User-Id: c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f`, `X-Role: ADMINISTRATOR` and
`X-Building-Id: 7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d` (the sample building of the `dev` profile).

### Example

```json
{
  "id": "6f1d2c3b-4a5e-4f6d-8c7b-9a0e1f2d3c4b",
  "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
  "type": "SEISMIC_RISK",
  "severity": "CRITICAL",
  "status": "OPEN",
  "title": "Anomalía estructural crítica tras un sismo",
  "message": "Anomalía estructural crítica detectada tras un sismo. Fuente: reporte del IGP (evento 9e8d7c6b-5a4f-4e3d-b2c1-0a9f8e7d6c5b) y sensores del edificio. Detectado: 30/10/2026 09:32 (hora de Lima). Siga el plan de evacuación de su edificio y las indicaciones de las autoridades. Rumi complementa al SASPe; no es una alerta oficial.",
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

The listener consumes this event from RabbitMQ. Seismic Correlation does not publish it yet.

- Exchange: `rumi.seismic-correlation.events` (topic, durable)
- Queue: `rumi.notification.correlated-seismic-event-created` (durable)
- Routing key: `seismic-correlation.event.correlated`
- Connection: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME` and `RABBITMQ_PASSWORD`

```json
{ "buildingId": "...", "riskIndexId": "...", "seismicEventId": "...", "riskLevel": "HIGH", "calculatedAt": "2026-10-30T15:30:00Z" }
```

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
