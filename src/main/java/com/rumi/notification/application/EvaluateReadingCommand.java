package com.rumi.notification.application;

import java.time.Instant;
import java.util.UUID;

/**
 * A sensor reading to compare with the threshold of its sensor (US15). It has the same fields as
 * the SensorReadingRecorded event of Structural Monitoring, so a future listener can pass it unchanged.
 */
public record EvaluateReadingCommand(
        UUID sensorId,
        UUID buildingId,
        String zone,
        double value,
        Instant recordedAt
) {
    public EvaluateReadingCommand {
        if (sensorId == null) {
            throw new IllegalArgumentException("Sensor id is required");
        }
        if (buildingId == null) {
            throw new IllegalArgumentException("Building id is required");
        }
        if (recordedAt == null) {
            throw new IllegalArgumentException("Recording time is required");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Reading value must be a finite number");
        }
    }
}
