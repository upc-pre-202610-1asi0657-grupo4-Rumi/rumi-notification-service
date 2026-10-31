package com.rumi.notification.application;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.AlertType;
import com.rumi.notification.domain.repository.AlertRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class InMemoryAlertRepository implements AlertRepository {

    private final Map<UUID, Alert> alerts = new LinkedHashMap<>();

    @Override
    public Optional<Alert> findById(UUID id) {
        return Optional.ofNullable(alerts.get(id));
    }

    @Override
    public List<Alert> findByBuildingId(UUID buildingId, AlertStatus status) {
        return alerts.values().stream()
                .filter(alert -> alert.getBuildingId().equals(buildingId))
                .filter(alert -> status == null || alert.getStatus() == status)
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public Optional<Alert> findByRiskIndexId(UUID riskIndexId) {
        return alerts.values().stream()
                .filter(alert -> Objects.equals(alert.getRiskIndexId(), riskIndexId))
                .findFirst();
    }

    @Override
    public boolean existsActiveThresholdAlert(UUID sensorId) {
        return alerts.values().stream()
                .anyMatch(alert -> alert.getType() == AlertType.THRESHOLD_EXCEEDED
                        && sensorId.equals(alert.getSensorId())
                        && alert.isActive());
    }

    @Override
    public Alert save(Alert alert) {
        alerts.put(alert.getId(), alert);
        return alert;
    }

    List<Alert> saved() {
        return List.copyOf(alerts.values());
    }
}
