package com.rumi.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSensorThresholdRepository extends JpaRepository<SensorThresholdJpaEntity, UUID> {

    List<SensorThresholdJpaEntity> findByBuildingIdOrderBySensorIdAsc(UUID buildingId);
}
