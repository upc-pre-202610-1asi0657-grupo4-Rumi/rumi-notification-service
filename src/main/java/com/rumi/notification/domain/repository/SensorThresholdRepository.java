package com.rumi.notification.domain.repository;

import com.rumi.notification.domain.model.SensorThreshold;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorThresholdRepository {

    Optional<SensorThreshold> findBySensorId(UUID sensorId);

    /**
     * Thresholds of the sensors of a building, ordered by sensor id.
     */
    List<SensorThreshold> findByBuildingId(UUID buildingId);

    SensorThreshold save(SensorThreshold threshold);
}
