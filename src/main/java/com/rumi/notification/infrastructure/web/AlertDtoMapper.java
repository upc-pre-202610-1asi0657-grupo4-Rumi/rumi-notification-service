package com.rumi.notification.infrastructure.web;

import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.infrastructure.web.dto.AlertResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AlertDtoMapper {

    public AlertResponseDto toResponse(Alert alert) {
        return new AlertResponseDto(
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
}
