package com.rumi.notification.infrastructure.persistence;

import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAlertRepository extends JpaRepository<AlertJpaEntity, UUID> {

    List<AlertJpaEntity> findByBuildingIdOrderByCreatedAtDescIdAsc(UUID buildingId);

    List<AlertJpaEntity> findByBuildingIdAndStatusOrderByCreatedAtDescIdAsc(UUID buildingId, AlertStatus status);

    Optional<AlertJpaEntity> findByRiskIndexId(UUID riskIndexId);

    boolean existsBySensorIdAndTypeAndStatusIn(UUID sensorId, AlertType type, Collection<AlertStatus> statuses);
}
