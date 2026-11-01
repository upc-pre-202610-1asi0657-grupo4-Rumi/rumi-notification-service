package com.rumi.notification.infrastructure.persistence;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.AlertType;
import com.rumi.notification.domain.repository.AlertRepository;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAlertRepository implements AlertRepository {

    private static final EnumSet<AlertStatus> ACTIVE_STATUSES = EnumSet.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED);

    private final SpringDataAlertRepository springDataRepository;

    public JpaAlertRepository(SpringDataAlertRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<Alert> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Alert> findByBuildingId(UUID buildingId, AlertStatus status) {
        List<AlertJpaEntity> entities = status == null
                ? springDataRepository.findByBuildingIdOrderByCreatedAtDescIdAsc(buildingId)
                : springDataRepository.findByBuildingIdAndStatusOrderByCreatedAtDescIdAsc(buildingId, status);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Alert> findByRiskIndexId(UUID riskIndexId) {
        return springDataRepository.findByRiskIndexId(riskIndexId).map(this::toDomain);
    }

    @Override
    public boolean existsActiveThresholdAlert(UUID sensorId) {
        return springDataRepository.existsBySensorIdAndTypeAndStatusIn(
                sensorId, AlertType.THRESHOLD_EXCEEDED, ACTIVE_STATUSES);
    }

    @Override
    public Alert save(Alert alert) {
        return toDomain(springDataRepository.save(toEntity(alert)));
    }

    private AlertJpaEntity toEntity(Alert alert) {
        return new AlertJpaEntity(
                alert.getId(),
                alert.getBuildingId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getMessage(),
                alert.isEvacuationRequired(),
                alert.getRiskIndexId(),
                alert.getSeismicEventId(),
                alert.getSensorId(),
                alert.getZone(),
                alert.getMeasuredValue(),
                alert.getOccurredAt(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy(),
                alert.getResolvedAt()
        );
    }

    private Alert toDomain(AlertJpaEntity entity) {
        return new Alert(
                entity.getId(),
                entity.getBuildingId(),
                entity.getType(),
                entity.getSeverity(),
                entity.getTitle(),
                entity.getMessage(),
                entity.isEvacuationRequired(),
                entity.getRiskIndexId(),
                entity.getSeismicEventId(),
                entity.getSensorId(),
                entity.getZone(),
                entity.getMeasuredValue(),
                entity.getOccurredAt(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getAcknowledgedAt(),
                entity.getAcknowledgedBy(),
                entity.getResolvedAt()
        );
    }
}
