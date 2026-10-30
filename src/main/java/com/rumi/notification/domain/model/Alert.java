package com.rumi.notification.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * An alert about one building. It is raised by a seismic risk index (US14, US16) or by a sensor
 * reading that exceeds its threshold (US15), and moves from OPEN to ACKNOWLEDGED to RESOLVED.
 */
public final class Alert {

    public static final int MAX_TITLE_LENGTH = 120;
    public static final int MAX_MESSAGE_LENGTH = 500;
    public static final int MAX_ZONE_LENGTH = 50;

    private final UUID id;
    private final UUID buildingId;
    private final AlertType type;
    private final AlertSeverity severity;
    private final String title;
    private final String message;
    private final boolean evacuationRequired;
    private final UUID riskIndexId;
    private final UUID seismicEventId;
    private final UUID sensorId;
    private final String zone;
    private final Double measuredValue;
    private final Instant occurredAt;
    private final Instant createdAt;
    private AlertStatus status;
    private Instant acknowledgedAt;
    private UUID acknowledgedBy;
    private Instant resolvedAt;

    @SuppressWarnings("java:S107")
    public Alert(
            UUID id,
            UUID buildingId,
            AlertType type,
            AlertSeverity severity,
            String title,
            String message,
            boolean evacuationRequired,
            UUID riskIndexId,
            UUID seismicEventId,
            UUID sensorId,
            String zone,
            Double measuredValue,
            Instant occurredAt,
            Instant createdAt,
            AlertStatus status,
            Instant acknowledgedAt,
            UUID acknowledgedBy,
            Instant resolvedAt
    ) {
        this.id = Objects.requireNonNull(id, "Alert id is required");
        this.buildingId = Objects.requireNonNull(buildingId, "Building id is required");
        this.type = Objects.requireNonNull(type, "Alert type is required");
        this.severity = Objects.requireNonNull(severity, "Alert severity is required");
        this.title = requireText(title, "Alert title", MAX_TITLE_LENGTH);
        this.message = requireText(message, "Alert message", MAX_MESSAGE_LENGTH);
        this.evacuationRequired = evacuationRequired;
        this.riskIndexId = riskIndexId;
        this.seismicEventId = seismicEventId;
        this.sensorId = sensorId;
        if (zone != null && zone.length() > MAX_ZONE_LENGTH) {
            throw new IllegalArgumentException("Zone must have at most " + MAX_ZONE_LENGTH + " characters");
        }
        this.zone = zone;
        this.measuredValue = measuredValue;
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurrence time is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required");
        this.status = Objects.requireNonNull(status, "Alert status is required");
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.resolvedAt = resolvedAt;
        if (type == AlertType.SEISMIC_RISK && riskIndexId == null) {
            throw new IllegalArgumentException("A seismic risk alert requires the risk index id");
        }
        if (type == AlertType.THRESHOLD_EXCEEDED && (sensorId == null || measuredValue == null)) {
            throw new IllegalArgumentException("A threshold alert requires the sensor id and the measured value");
        }
    }

    /**
     * Raises the alert of a risk index calculated by Seismic Correlation. A CRITICAL risk requires
     * the residents to evacuate (US16).
     */
    public static Alert fromSeismicRisk(
            UUID id,
            UUID buildingId,
            UUID riskIndexId,
            UUID seismicEventId,
            AlertSeverity severity,
            Instant calculatedAt,
            Instant createdAt
    ) {
        Objects.requireNonNull(severity, "Alert severity is required");
        return new Alert(
                id, buildingId, AlertType.SEISMIC_RISK, severity,
                "Seismic risk " + severity.name(),
                seismicMessage(severity),
                severity == AlertSeverity.CRITICAL,
                riskIndexId, seismicEventId, null, null, null,
                calculatedAt, createdAt, AlertStatus.OPEN, null, null, null
        );
    }

    /**
     * Raises the alert of a sensor reading that exceeded its threshold (US15). The severity comes
     * from {@link SensorThreshold#classify(double)}.
     */
    public static Alert fromThresholdExceeded(
            UUID id,
            SensorThreshold threshold,
            AlertSeverity severity,
            String zone,
            double measuredValue,
            Instant recordedAt,
            Instant createdAt
    ) {
        Objects.requireNonNull(threshold, "Sensor threshold is required");
        Objects.requireNonNull(severity, "Alert severity is required");
        String limitName = severity == AlertSeverity.CRITICAL ? "critical" : "warning";
        String where = zone == null || zone.isBlank() ? "" : " in zone " + zone;
        String message = String.format(Locale.ROOT,
                "Reading %.3f%s reached the %s threshold %.3f of sensor %s.",
                measuredValue, where, limitName, threshold.limitFor(severity), threshold.getSensorId());
        return new Alert(
                id, threshold.getBuildingId(), AlertType.THRESHOLD_EXCEEDED, severity,
                "Sensor threshold exceeded",
                message,
                false,
                null, null, threshold.getSensorId(), zone, measuredValue,
                recordedAt, createdAt, AlertStatus.OPEN, null, null, null
        );
    }

    /**
     * Records that an administrator has seen the alert. Only an OPEN alert can be acknowledged.
     */
    public void acknowledge(UUID userId, Instant at) {
        if (status != AlertStatus.OPEN) {
            throw new AlertStateException("Alert " + id + " is " + status + " and cannot be acknowledged");
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Objects.requireNonNull(at, "Acknowledgement time is required");
        this.acknowledgedBy = userId;
    }

    /**
     * Closes the alert. An OPEN or ACKNOWLEDGED alert can be resolved, but only once.
     */
    public void resolve(Instant at) {
        if (status == AlertStatus.RESOLVED) {
            throw new AlertStateException("Alert " + id + " is already RESOLVED");
        }
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = Objects.requireNonNull(at, "Resolution time is required");
    }

    public boolean isActive() {
        return status != AlertStatus.RESOLVED;
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

    public AlertStatus getStatus() {
        return status;
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

    private static String seismicMessage(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "Critical structural risk after the seismic event. "
                    + "Evacuate the building now using the nearest evacuation route.";
            case HIGH -> "High structural risk after the seismic event. "
                    + "Stay alert and follow the instructions of the building administration.";
            case MEDIUM -> "Medium structural risk after the seismic event. "
                    + "A structural inspection of the affected zones is recommended.";
            case LOW -> "Seismic event detected with low structural risk. No action is required.";
        };
    }

    private static String requireText(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(name + " must have at most " + maxLength + " characters");
        }
        return value;
    }
}
