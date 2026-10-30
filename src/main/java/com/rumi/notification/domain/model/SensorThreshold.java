package com.rumi.notification.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Alert limits of one sensor (US15). A reading at or above the warning value raises a HIGH alert;
 * at or above the critical value, a CRITICAL alert. Each sensor has at most one threshold.
 */
public final class SensorThreshold {

    private final UUID sensorId;
    private final UUID buildingId;
    private double warningValue;
    private double criticalValue;
    private Instant updatedAt;

    public SensorThreshold(UUID sensorId, UUID buildingId, double warningValue, double criticalValue,
                           Instant updatedAt) {
        this.sensorId = Objects.requireNonNull(sensorId, "Sensor id is required");
        this.buildingId = Objects.requireNonNull(buildingId, "Building id is required");
        requireValidLimits(warningValue, criticalValue);
        this.warningValue = warningValue;
        this.criticalValue = criticalValue;
        this.updatedAt = Objects.requireNonNull(updatedAt, "Update time is required");
    }

    /**
     * Changes the limits of the threshold.
     */
    public void changeLimits(double newWarningValue, double newCriticalValue, Instant changedAt) {
        requireValidLimits(newWarningValue, newCriticalValue);
        this.warningValue = newWarningValue;
        this.criticalValue = newCriticalValue;
        this.updatedAt = Objects.requireNonNull(changedAt, "Update time is required");
    }

    /**
     * Returns the severity of an alert for a reading, or empty when the reading is below the warning value.
     */
    public Optional<AlertSeverity> classify(double readingValue) {
        if (!Double.isFinite(readingValue)) {
            throw new IllegalArgumentException("Reading value must be a finite number");
        }
        if (readingValue >= criticalValue) {
            return Optional.of(AlertSeverity.CRITICAL);
        }
        if (readingValue >= warningValue) {
            return Optional.of(AlertSeverity.HIGH);
        }
        return Optional.empty();
    }

    /**
     * Returns the limit that a reading of the given severity exceeded.
     */
    public double limitFor(AlertSeverity severity) {
        return severity == AlertSeverity.CRITICAL ? criticalValue : warningValue;
    }

    public boolean belongsTo(UUID otherBuildingId) {
        return buildingId.equals(otherBuildingId);
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public UUID getBuildingId() {
        return buildingId;
    }

    public double getWarningValue() {
        return warningValue;
    }

    public double getCriticalValue() {
        return criticalValue;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static void requireValidLimits(double warningValue, double criticalValue) {
        if (!Double.isFinite(warningValue) || !Double.isFinite(criticalValue)) {
            throw new IllegalArgumentException("Threshold values must be finite numbers");
        }
        if (warningValue <= 0) {
            throw new IllegalArgumentException("Warning value must be greater than 0");
        }
        if (warningValue >= criticalValue) {
            throw new IllegalArgumentException("Warning value must be lower than the critical value");
        }
    }
}
