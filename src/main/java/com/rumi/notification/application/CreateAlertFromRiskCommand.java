package com.rumi.notification.application;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Data of a risk index calculated by Seismic Correlation. The RabbitMQ listener of this service
 * builds it from the risk event and passes it to
 * {@link AlertApplicationService#createAlertFromRisk(CreateAlertFromRiskCommand)}.
 *
 * @param buildingId     building whose risk was calculated
 * @param riskIndexId    risk index of Seismic Correlation; a second command with the same id is ignored
 * @param seismicEventId seismic event reported by the IGP
 * @param riskLevel      LOW, MEDIUM, HIGH or CRITICAL
 * @param calculatedAt   when Seismic Correlation calculated the risk index
 */
public record CreateAlertFromRiskCommand(
        UUID buildingId,
        UUID riskIndexId,
        UUID seismicEventId,
        String riskLevel,
        Instant calculatedAt
) {
    public CreateAlertFromRiskCommand {
        requireValue(buildingId, "Building id is required");
        requireValue(riskIndexId, "Risk index id is required");
        requireValue(seismicEventId, "Seismic event id is required");
        requireValue(riskLevel, "Risk level is required");
        requireValue(calculatedAt, "Calculation time is required");
    }

    private static void requireValue(Object value, String message) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
