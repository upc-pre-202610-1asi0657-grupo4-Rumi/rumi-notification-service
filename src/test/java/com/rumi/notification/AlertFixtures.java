package com.rumi.notification;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.SensorThreshold;

import java.time.Instant;
import java.util.UUID;

public final class AlertFixtures {

    public static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    public static final UUID SENSOR_ID = UUID.fromString("5d1c2f0e-8f4a-4a53-9a7e-0f3b1c9d7a11");
    public static final Instant CALCULATED_AT = Instant.parse("2026-10-30T14:32:00Z");
    public static final Instant NOW = Instant.parse("2026-10-30T14:32:04Z");

    private AlertFixtures() {
    }

    public static Alert seismicAlert(AlertSeverity severity) {
        return seismicAlert(BUILDING_ID, severity, NOW);
    }

    public static Alert seismicAlert(UUID buildingId, AlertSeverity severity, Instant createdAt) {
        return Alert.fromSeismicRisk(UUID.randomUUID(), buildingId, UUID.randomUUID(), UUID.randomUUID(),
                severity, CALCULATED_AT, createdAt);
    }

    public static SensorThreshold threshold() {
        return new SensorThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10, NOW);
    }

    public static Alert thresholdAlert() {
        return Alert.fromThresholdExceeded(UUID.randomUUID(), threshold(), AlertSeverity.CRITICAL,
                "FLOOR-3-NORTH", 0.12, CALCULATED_AT, NOW);
    }
}
