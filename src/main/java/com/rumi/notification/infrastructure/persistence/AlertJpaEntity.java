package com.rumi.notification.infrastructure.persistence;

import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class AlertJpaEntity {

    @Id
    private UUID id;

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "evacuation_required", nullable = false)
    private boolean evacuationRequired;

    @Column(name = "risk_index_id", unique = true)
    private UUID riskIndexId;

    @Column(name = "seismic_event_id")
    private UUID seismicEventId;

    @Column(name = "sensor_id")
    private UUID sensorId;

    @Column(length = 50)
    private String zone;

    @Column(name = "measured_value")
    private Double measuredValue;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected AlertJpaEntity() {
    }

    @SuppressWarnings("java:S107")
    public AlertJpaEntity(UUID id, UUID buildingId, AlertType type, AlertSeverity severity, AlertStatus status,
                          String title, String message, boolean evacuationRequired, UUID riskIndexId,
                          UUID seismicEventId, UUID sensorId, String zone, Double measuredValue,
                          Instant occurredAt, Instant createdAt, Instant acknowledgedAt, UUID acknowledgedBy,
                          Instant resolvedAt) {
        this.id = id;
        this.buildingId = buildingId;
        this.type = type;
        this.severity = severity;
        this.status = status;
        this.title = title;
        this.message = message;
        this.evacuationRequired = evacuationRequired;
        this.riskIndexId = riskIndexId;
        this.seismicEventId = seismicEventId;
        this.sensorId = sensorId;
        this.zone = zone;
        this.measuredValue = measuredValue;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.resolvedAt = resolvedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuildingId() {
        return buildingId;
    }

    public AlertType getType() {
        return type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEvacuationRequired() {
        return evacuationRequired;
    }

    public UUID getRiskIndexId() {
        return riskIndexId;
    }

    public UUID getSeismicEventId() {
        return seismicEventId;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public String getZone() {
        return zone;
    }

    public Double getMeasuredValue() {
        return measuredValue;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public UUID getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
