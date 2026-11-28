package com.rumi.notification.domain.model;

import com.rumi.notification.AlertFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertTest {

    /**
     * Any order to evacuate, in English or Spanish ("Evacuate", "evacúe", "evacuar", "evacuen"). It does not match
     * the noun in "plan de evacuación": pointing to the building's evacuation plan is what the alerts must do.
     */
    private static final Pattern EVACUATION_ORDER =
            Pattern.compile("(?iU)\\bevac(?:uate|úe|ue|uar|uen)\\b");
    /** CALCULATED_AT (2026-10-30T14:32:00Z) in Lima time. */
    private static final String DETECTED_IN_LIMA = "Detectado: 30/10/2026 09:32 (hora de Lima)";

    @Test
    void aCriticalSeismicRiskRaisesAnOpenAlertThatPointsToTheEvacuationPlan() {
        UUID seismicEventId = UUID.randomUUID();
        Alert alert = Alert.fromSeismicRisk(UUID.randomUUID(), AlertFixtures.BUILDING_ID, UUID.randomUUID(),
                seismicEventId, AlertSeverity.CRITICAL, AlertFixtures.CALCULATED_AT, AlertFixtures.NOW);

        assertThat(alert.getType()).isEqualTo(AlertType.SEISMIC_RISK);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.isEvacuationRequired()).isTrue();
        assertThat(alert.getTitle()).isEqualTo("Anomalía estructural crítica tras un sismo");
        assertThat(alert.getMessage())
                .isEqualTo("Anomalía estructural crítica detectada tras un sismo. Fuente: reporte del IGP (evento "
                        + seismicEventId + ") y sensores del edificio. " + DETECTED_IN_LIMA + ". Siga el plan de "
                        + "evacuación de su edificio y las indicaciones de las autoridades. Rumi complementa al "
                        + "SASPe; no es una alerta oficial.");
        assertThat(alert.getOccurredAt()).isEqualTo(AlertFixtures.CALCULATED_AT);
        assertThat(alert.getCreatedAt()).isEqualTo(AlertFixtures.NOW);
    }

    @Test
    void noAlertOrdersToEvacuate() {
        for (AlertSeverity severity : AlertSeverity.values()) {
            Alert alert = AlertFixtures.seismicAlert(severity);
            assertThat(alert.getMessage()).as("seismic %s", severity).doesNotContainPattern(EVACUATION_ORDER);
            assertThat(alert.getTitle()).as("seismic %s", severity).doesNotContainPattern(EVACUATION_ORDER);
        }
        for (AlertSeverity severity : new AlertSeverity[]{AlertSeverity.HIGH, AlertSeverity.CRITICAL}) {
            Alert alert = Alert.fromThresholdExceeded(UUID.randomUUID(), AlertFixtures.threshold(), severity,
                    "FLOOR-3-NORTH", 0.12, AlertFixtures.CALCULATED_AT, AlertFixtures.NOW);
            assertThat(alert.getMessage()).as("threshold %s", severity).doesNotContainPattern(EVACUATION_ORDER);
        }
    }

    @Test
    void everySeismicAlertStatesTheAnomalyItsSourceAndWhenItWasDetected() {
        for (AlertSeverity severity : AlertSeverity.values()) {
            String message = AlertFixtures.seismicAlert(severity).getMessage();

            assertThat(message).as("seismic %s", severity)
                    .startsWith("Anomalía estructural ")
                    .contains("Fuente: reporte del IGP (evento ")
                    .contains(DETECTED_IN_LIMA)
                    .contains("indicaciones de las autoridades")
                    .hasSizeLessThanOrEqualTo(Alert.MAX_MESSAGE_LENGTH);
        }
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.HIGH).getMessage())
                .contains("Siga el plan de evacuación de su edificio");
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.MEDIUM).getMessage())
                .contains("Se recomienda inspeccionar las zonas afectadas");
    }

    @Test
    void theSeverityOfASeismicAlertIsNamedInSpanish() {
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.LOW).getTitle())
                .isEqualTo("Anomalía estructural baja tras un sismo");
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.MEDIUM).getTitle())
                .isEqualTo("Anomalía estructural media tras un sismo");
        assertThat(AlertFixtures.seismicAlert(AlertSeverity.HIGH).getTitle())
                .isEqualTo("Anomalía estructural alta tras un sismo");
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
        assertThat(alert.getTitle()).isEqualTo("Lectura fuera de umbral");
        assertThat(alert.getMessage())
                .isEqualTo("Anomalía detectada por el sensor " + AlertFixtures.SENSOR_ID + " en la zona "
                        + "FLOOR-3-NORTH: lectura 0,120 sobre el umbral crítico 0,100. " + DETECTED_IN_LIMA
                        + ". Siga el plan de su edificio y las indicaciones de las autoridades.");
    }

    @Test
    void aWarningThresholdAlertWithoutZoneNamesTheWarningLimit() {
        Alert alert = Alert.fromThresholdExceeded(UUID.randomUUID(), AlertFixtures.threshold(), AlertSeverity.HIGH,
                null, 0.07, AlertFixtures.CALCULATED_AT, AlertFixtures.NOW);

        assertThat(alert.getMessage())
                .isEqualTo("Anomalía detectada por el sensor " + AlertFixtures.SENSOR_ID + ": lectura 0,070 sobre "
                        + "el umbral de advertencia 0,050. " + DETECTED_IN_LIMA
                        + ". Siga el plan de su edificio y las indicaciones de las autoridades.");
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
