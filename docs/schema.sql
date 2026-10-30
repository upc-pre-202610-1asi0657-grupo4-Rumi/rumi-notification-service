-- Schema of notificationsdb (PostgreSQL), the database of rumi-notification-service.
-- The default profile runs Hibernate with ddl-auto=validate, so these tables must exist.
-- Create the database first:  CREATE DATABASE notificationsdb;
-- Buildings, sensors, risk indexes and seismic events belong to other services: they are
-- referenced by identifier only, without foreign keys (one database per service).

CREATE TABLE alerts (
    id                  uuid             PRIMARY KEY,
    building_id         uuid             NOT NULL,
    type                varchar(30)      NOT NULL CHECK (type IN ('SEISMIC_RISK', 'THRESHOLD_EXCEEDED')),
    severity            varchar(20)      NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status              varchar(20)      NOT NULL CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED')),
    title               varchar(120)     NOT NULL,
    message             varchar(500)     NOT NULL,
    evacuation_required boolean          NOT NULL,
    risk_index_id       uuid             UNIQUE,
    seismic_event_id    uuid,
    sensor_id           uuid,
    zone                varchar(50),
    measured_value      double precision,
    occurred_at         timestamp        NOT NULL,
    created_at          timestamp        NOT NULL,
    acknowledged_at     timestamp,
    acknowledged_by     uuid,
    resolved_at         timestamp
);

CREATE TABLE sensor_thresholds (
    sensor_id      uuid             PRIMARY KEY,
    building_id    uuid             NOT NULL,
    warning_value  double precision NOT NULL CHECK (warning_value > 0),
    critical_value double precision NOT NULL,
    updated_at     timestamp        NOT NULL,
    CHECK (warning_value < critical_value)
);

CREATE INDEX alerts_building_id_created_at_idx ON alerts (building_id, created_at DESC);
CREATE INDEX alerts_sensor_id_idx ON alerts (sensor_id);
CREATE INDEX sensor_thresholds_building_id_idx ON sensor_thresholds (building_id);
