package com.rumi.notification.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Risk event of Seismic Correlation, with only the fields this service reads.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CorrelatedSeismicEventCreated(
        UUID buildingId,
        UUID riskIndexId,
        UUID seismicEventId,
        String riskLevel,
        Instant calculatedAt
) {
}
