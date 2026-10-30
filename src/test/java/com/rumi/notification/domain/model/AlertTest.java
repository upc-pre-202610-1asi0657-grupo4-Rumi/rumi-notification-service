package com.rumi.notification.domain.model;

import com.rumi.notification.AlertFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertTest {

    @Test
    void aCriticalSeismicRiskRaisesAnOpenEvacuationAlert() {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.CRITICAL);

        assertThat(alert.getType()).isEqualTo(AlertType.SEISMIC_RISK);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.isEvacuationRequired()).isTrue();
        assertThat(alert.getTitle()).isEqualTo("Seismic risk CRITICAL");
        assertThat(alert.getMessage()).contains("Evacuate");
        assertThat(alert.getOccurredAt()).isEqualTo(AlertFixtures.CALCULATED_AT);
        assertThat(alert.getCreatedAt()).isEqualTo(AlertFixtures.NOW);
    }

    @Test
    void aSeismicRiskBelowCriticalDoesNotRequireEvacuation() {
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.LOW).isEvacuationRequired()).isFalse();
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.MEDIUM).isEvacuationRequired()).isFalse();
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.HIGH).isEvacuationRequired()).isFalse();
    }

    @Test
    void aThresholdAlertNamesTheReadingTheZoneAndTheExceededLimit() {
        Alert alert = AlertFixtures.thresholdAlert();

        assertThat(alert.getType()).isEqualTo(AlertType.THRESHOLD_EXCEEDED);
        assertThat(alert.getBuildingId()).isEqualTo(AlertFixtures.BUILDING_ID);
        assertThat(alert.getSensorId()).isEqualTo(AlertFixtures.SENSOR_ID);
        assertThat(alert.getMeasuredValue()).isEqualTo(0.12);
        assertThat(alert.isEvacuationRequired()).isFalse();
        assertThat(alert.getMessage())
                .isEqualTo("Reading 0.120 in zone FLOOR-3-NORTH reached the critical threshold 0.100 of sensor "
                        + AlertFixtures.SENSOR_ID + ".");
    }

    @Test
    void anOpenAlertCanBeAcknowledged() {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.HIGH);
        UUID userId = UUID.randomUUID();
        Instant at = AlertFixtures.NOW.plusSeconds(30);

        alert.acknowledge(userId, at);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedBy()).isEqualTo(userId);
        assertThat(alert.getAcknowledgedAt()).isEqualTo(at);
        assertThat(alert.isActive()).isTrue();
    }

    @Test
    void anAlertCannotBeAcknowledgedTwice() {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.HIGH);
        alert.acknowledge(null, AlertFixtures.NOW);

        assertThatThrownBy(() -> alert.acknowledge(null, AlertFixtures.NOW))
                .isInstanceOf(AlertStateException.class)
                .hasMessageContaining("is ACKNOWLEDGED and cannot be acknowledged");
    }

    @Test
    void anAcknowledgedAlertCanBeResolvedButOnlyOnce() {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.MEDIUM);
        alert.acknowledge(null, AlertFixtures.NOW);

        alert.resolve(AlertFixtures.NOW.plusSeconds(60));

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(alert.isActive()).isFalse();
        assertThatThrownBy(() -> alert.resolve(AlertFixtures.NOW))
                .isInstanceOf(AlertStateException.class)
                .hasMessageContaining("is already RESOLVED");
    }

    @Test
    void aResolvedAlertCannotBeAcknowledged() {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.MEDIUM);
        alert.resolve(AlertFixtures.NOW);

        assertThatThrownBy(() -> alert.acknowledge(null, AlertFixtures.NOW))
                .isInstanceOf(AlertStateException.class);
    }

    @Test
    void aSeismicAlertRequiresItsRiskIndex() {
        assertThatThrownBy(() -> Alert.fromSeismicRisk(UUID.randomUUID(), AlertFixtures.BUILDING_ID, null,
                UUID.randomUUID(), AlertSeverity.LOW, AlertFixtures.CALCULATED_AT, AlertFixtures.NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A seismic risk alert requires the risk index id");
    }

    @Test
    void aZoneLongerThanFiftyCharactersIsRejected() {
        String longZone = "Z".repeat(Alert.MAX_ZONE_LENGTH + 1);

        assertThatThrownBy(() -> Alert.fromThresholdExceeded(UUID.randomUUID(), AlertFixtures.threshold(),
                AlertSeverity.HIGH, longZone, 0.07, AlertFixtures.CALCULATED_AT, AlertFixtures.NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
