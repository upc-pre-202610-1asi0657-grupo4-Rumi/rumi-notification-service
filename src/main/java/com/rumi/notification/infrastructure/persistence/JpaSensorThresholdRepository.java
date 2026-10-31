package com.rumi.notification.infrastructure.persistence;

import com.rumi.notification.domain.model.SensorThreshold;
import com.rumi.notification.domain.repository.SensorThresholdRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaSensorThresholdRepository implements SensorThresholdRepository {

    private final SpringDataSensorThresholdRepository springDataRepository;

    public JpaSensorThresholdRepository(SpringDataSensorThresholdRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<SensorThreshold> findBySensorId(UUID sensorId) {
        return springDataRepository.findById(sensorId).map(this::toDomain);
    }

    @Override
    public List<SensorThreshold> findByBuildingId(UUID buildingId) {
        return springDataRepository.findByBuildingIdOrderBySensorIdAsc(buildingId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public SensorThreshold save(SensorThreshold threshold) {
        return toDomain(springDataRepository.save(new SensorThresholdJpaEntity(
                threshold.getSensorId(),
                threshold.getBuildingId(),
                threshold.getWarningValue(),
                threshold.getCriticalValue(),
                threshold.getUpdatedAt()
        )));
    }

    private SensorThreshold toDomain(SensorThresholdJpaEntity entity) {
        return new SensorThreshold(
                entity.getSensorId(),
                entity.getBuildingId(),
                entity.getWarningValue(),
                entity.getCriticalValue(),
                entity.getUpdatedAt()
        );
    }
}
