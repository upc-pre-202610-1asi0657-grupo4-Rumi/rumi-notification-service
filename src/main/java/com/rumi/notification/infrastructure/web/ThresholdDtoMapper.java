package com.rumi.notification.infrastructure.web;

import com.rumi.notification.domain.model.SensorThreshold;
import com.rumi.notification.infrastructure.web.dto.ThresholdResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ThresholdDtoMapper {

    public ThresholdResponseDto toResponse(SensorThreshold threshold) {
        return new ThresholdResponseDto(
                threshold.getSensorId(),
                threshold.getBuildingId(),
                threshold.getWarningValue(),
                threshold.getCriticalValue(),
                threshold.getUpdatedAt()
        );
    }
}
