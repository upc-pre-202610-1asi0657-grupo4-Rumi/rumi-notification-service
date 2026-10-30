package com.rumi.notification.application;

import com.rumi.notification.domain.model.SensorThreshold;
import com.rumi.notification.domain.repository.SensorThresholdRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class InMemorySensorThresholdRepository implements SensorThresholdRepository {

    private final Map<UUID, SensorThreshold> thresholds = new LinkedHashMap<>();

    @Override
    public Optional<SensorThreshold> findBySensorId(UUID sensorId) {
        return Optional.ofNullable(thresholds.get(sensorId));
    }

    @Override
    public List<SensorThreshold> findByBuildingId(UUID buildingId) {
        return thresholds.values().stream()
                .filter(threshold -> threshold.getBuildingId().equals(buildingId))
                .sorted(Comparator.comparing(SensorThreshold::getSensorId))
                .toList();
    }

    @Override
    public SensorThreshold save(SensorThreshold threshold) {
        thresholds.put(threshold.getSensorId(), threshold);
        return threshold;
    }
}
