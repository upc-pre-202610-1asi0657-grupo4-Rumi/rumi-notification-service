package com.rumi.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensor_thresholds")
public class SensorThresholdJpaEntity {

    @Id
    @Column(name = "sensor_id")
    private UUID sensorId;

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "warning_value", nullable = false)
    private double warningValue;

    @Column(name = "critical_value", nullable = false)
    private double criticalValue;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SensorThresholdJpaEntity() {
    }

    public SensorThresholdJpaEntity(UUID sensorId, UUID buildingId, double warningValue, double criticalValue,
                                    Instant updatedAt) {
        this.sensorId = sensorId;
        this.buildingId = buildingId;
        this.warningValue = warningValue;
        this.criticalValue = criticalValue;
        this.updatedAt = updatedAt;
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
}
