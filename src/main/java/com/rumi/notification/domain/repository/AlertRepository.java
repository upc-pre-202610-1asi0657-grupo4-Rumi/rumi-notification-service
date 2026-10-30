package com.rumi.notification.domain.repository;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository {

    Optional<Alert> findById(UUID id);

    /**
     * Alerts of a building, newest first. When {@code status} is null, alerts of every status are returned.
     */
    List<Alert> findByBuildingId(UUID buildingId, AlertStatus status);

    /**
     * The alert already raised for a risk index, used to ignore a risk event that arrives twice.
     */
    Optional<Alert> findByRiskIndexId(UUID riskIndexId);

    /**
     * Whether the sensor already has a threshold alert that is not resolved.
     */
    boolean existsActiveThresholdAlert(UUID sensorId);

    Alert save(Alert alert);
}
