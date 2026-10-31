package com.rumi.notification.application;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.SensorThreshold;
import com.rumi.notification.domain.repository.AlertRepository;
import com.rumi.notification.domain.repository.SensorThresholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AlertApplicationService {

    private final AlertRepository alertRepository;
    private final SensorThresholdRepository thresholdRepository;
    private final Clock clock;

    public AlertApplicationService(
            AlertRepository alertRepository,
            SensorThresholdRepository thresholdRepository,
            Clock clock
    ) {
        this.alertRepository = alertRepository;
        this.thresholdRepository = thresholdRepository;
        this.clock = clock;
    }

    /**
     * Raises the alert of a risk index calculated by Seismic Correlation (US14, US16). The RabbitMQ
     * listener of the risk event calls this method. It is idempotent: when the same risk index arrives
     * again, for example because the broker redelivers the message, the alert already raised is
     * returned and no new alert is created.
     */
    @Transactional
    public Alert createAlertFromRisk(CreateAlertFromRiskCommand command) {
        AlertSeverity severity = AlertSeverity.fromRiskLevel(command.riskLevel());
        return alertRepository.findByRiskIndexId(command.riskIndexId())
                .orElseGet(() -> alertRepository.save(Alert.fromSeismicRisk(
                        UUID.randomUUID(),
                        command.buildingId(),
                        command.riskIndexId(),
                        command.seismicEventId(),
                        severity,
                        command.calculatedAt(),
                        now()
                )));
    }

    /**
     * Compares a sensor reading with the threshold of its sensor (US15). Returns the alert raised, or
     * empty when the sensor has no threshold, the reading is below the warning value, or the sensor
     * already has an alert that is not resolved.
     */
    @Transactional
    public Optional<Alert> evaluateReading(EvaluateReadingCommand command) {
        Optional<SensorThreshold> threshold = thresholdRepository.findBySensorId(command.sensorId());
        if (threshold.isEmpty()) {
            return Optional.empty();
        }
        Optional<AlertSeverity> severity = threshold.get().classify(command.value());
        if (severity.isEmpty() || alertRepository.existsActiveThresholdAlert(command.sensorId())) {
            return Optional.empty();
        }
        Alert alert = Alert.fromThresholdExceeded(
                UUID.randomUUID(),
                threshold.get(),
                severity.get(),
                command.zone(),
                command.value(),
                command.recordedAt(),
                now()
        );
        return Optional.of(alertRepository.save(alert));
    }

    /**
     * Alerts of a building, newest first, optionally only those with one status.
     */
    public List<Alert> listAlerts(UUID buildingId, AlertStatus status) {
        if (buildingId == null) {
            throw new IllegalArgumentException("Building id is required");
        }
        return alertRepository.findByBuildingId(buildingId, status);
    }

    public Alert getAlert(UUID alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
    }

    /**
     * Records that a user has seen an OPEN alert. {@code userId} may be null when the request does not
     * come through the API gateway.
     */
    @Transactional
    public Alert acknowledgeAlert(UUID alertId, UUID userId) {
        Alert alert = getAlert(alertId);
        alert.acknowledge(userId, now());
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert resolveAlert(UUID alertId) {
        Alert alert = getAlert(alertId);
        alert.resolve(now());
        return alertRepository.save(alert);
    }

    /**
     * Creates the threshold of a sensor or changes its limits (US15). A sensor keeps the building it was
     * first configured with.
     */
    @Transactional
    public SensorThreshold configureThreshold(UUID sensorId, UUID buildingId, double warningValue,
                                              double criticalValue) {
        Optional<SensorThreshold> existing = thresholdRepository.findBySensorId(sensorId);
        if (existing.isEmpty()) {
            return thresholdRepository.save(
                    new SensorThreshold(sensorId, buildingId, warningValue, criticalValue, now()));
        }
        SensorThreshold threshold = existing.get();
        if (!threshold.belongsTo(buildingId)) {
            throw new IllegalArgumentException(
                    "Sensor " + sensorId + " belongs to building " + threshold.getBuildingId());
        }
        threshold.changeLimits(warningValue, criticalValue, now());
        return thresholdRepository.save(threshold);
    }

    public SensorThreshold getThreshold(UUID sensorId) {
        return thresholdRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new ThresholdNotFoundException(sensorId));
    }

    public List<SensorThreshold> listThresholds(UUID buildingId) {
        if (buildingId == null) {
            throw new IllegalArgumentException("Building id is required");
        }
        return thresholdRepository.findByBuildingId(buildingId);
    }

    private Instant now() {
        return clock.instant();
    }
}
