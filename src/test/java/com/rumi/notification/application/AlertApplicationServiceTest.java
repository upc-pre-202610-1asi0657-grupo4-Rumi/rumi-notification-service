package com.rumi.notification.application;

import com.rumi.notification.AlertFixtures;
import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.AlertStateException;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.AlertType;
import com.rumi.notification.domain.model.SensorThreshold;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertApplicationServiceTest {

    private static final UUID BUILDING_ID = AlertFixtures.BUILDING_ID;
    private static final UUID SENSOR_ID = AlertFixtures.SENSOR_ID;

    private final InMemoryAlertRepository alertRepository = new InMemoryAlertRepository();
    private final InMemorySensorThresholdRepository thresholdRepository = new InMemorySensorThresholdRepository();
    private final AlertApplicationService service = new AlertApplicationService(
            alertRepository, thresholdRepository, Clock.fixed(AlertFixtures.NOW, ZoneOffset.UTC));

    private CreateAlertFromRiskCommand risk(UUID riskIndexId, String level) {
        return new CreateAlertFromRiskCommand(BUILDING_ID, riskIndexId, UUID.randomUUID(), level,
                AlertFixtures.CALCULATED_AT);
    }

    private EvaluateReadingCommand reading(double value) {
        return new EvaluateReadingCommand(SENSOR_ID, BUILDING_ID, "FLOOR-3-NORTH", value, AlertFixtures.CALCULATED_AT);
    }

    @Test
    void createsAnOpenAlertFromARiskIndex() {
        UUID riskIndexId = UUID.randomUUID();

        Alert alert = service.createAlertFromRisk(risk(riskIndexId, "HIGH"));

        assertThat(alert.getBuildingId()).isEqualTo(BUILDING_ID);
        assertThat(alert.getRiskIndexId()).isEqualTo(riskIndexId);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getCreatedAt()).isEqualTo(AlertFixtures.NOW);
        assertThat(alertRepository.saved()).containsExactly(alert);
    }

    @Test
    void aCriticalRiskIndexRaisesAnEvacuationAlert() {
        Alert alert = service.createAlertFromRisk(risk(UUID.randomUUID(), "CRITICAL"));

        assertThat(alert.isEvacuationRequired()).isTrue();
    }

    @Test
    void theSameRiskIndexReceivedTwiceRaisesOnlyOneAlert() {
        UUID riskIndexId = UUID.randomUUID();

        Alert first = service.createAlertFromRisk(risk(riskIndexId, "MEDIUM"));
        Alert second = service.createAlertFromRisk(risk(riskIndexId, "MEDIUM"));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(alertRepository.saved()).hasSize(1);
    }

    @Test
    void rejectsAnUnknownRiskLevelWithoutSavingAnAlert() {
        assertThatThrownBy(() -> service.createAlertFromRisk(risk(UUID.randomUUID(), "SEVERE")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(alertRepository.saved()).isEmpty();
    }

    @Test
    void theRiskCommandRequiresEveryField() {
        assertThatThrownBy(() -> new CreateAlertFromRiskCommand(null, UUID.randomUUID(), UUID.randomUUID(),
                "LOW", AlertFixtures.CALCULATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Building id is required");
    }

    @Test
    void aReadingAboveTheCriticalValueRaisesACriticalThresholdAlert() {
        service.configureThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10);

        Optional<Alert> alert = service.evaluateReading(reading(0.12));

        assertThat(alert).hasValueSatisfying(raised -> {
            assertThat(raised.getType()).isEqualTo(AlertType.THRESHOLD_EXCEEDED);
            assertThat(raised.getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
            assertThat(raised.getSensorId()).isEqualTo(SENSOR_ID);
            assertThat(raised.getZone()).isEqualTo("FLOOR-3-NORTH");
        });
    }

    @Test
    void aReadingBelowTheWarningValueRaisesNoAlert() {
        service.configureThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10);

        assertThat(service.evaluateReading(reading(0.01))).isEmpty();
        assertThat(alertRepository.saved()).isEmpty();
    }

    @Test
    void aReadingOfASensorWithoutThresholdRaisesNoAlert() {
        assertThat(service.evaluateReading(reading(5.0))).isEmpty();
    }

    @Test
    void aSensorWithAnActiveThresholdAlertDoesNotRaiseAnotherOne() {
        service.configureThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10);
        Alert first = service.evaluateReading(reading(0.07)).orElseThrow();

        assertThat(service.evaluateReading(reading(0.12))).isEmpty();

        service.resolveAlert(first.getId());
        assertThat(service.evaluateReading(reading(0.12))).isPresent();
    }

    @Test
    void listsTheAlertsOfABuildingFilteredByStatus() {
        Alert open = service.createAlertFromRisk(risk(UUID.randomUUID(), "LOW"));
        Alert acknowledged = service.createAlertFromRisk(risk(UUID.randomUUID(), "HIGH"));
        service.acknowledgeAlert(acknowledged.getId(), null);
        alertRepository.save(AlertFixtures.seismicAlert(UUID.randomUUID(), AlertSeverity.LOW, AlertFixtures.NOW));

        assertThat(service.listAlerts(BUILDING_ID, null)).extracting(Alert::getId)
                .containsExactlyInAnyOrder(open.getId(), acknowledged.getId());
        assertThat(service.listAlerts(BUILDING_ID, AlertStatus.OPEN)).extracting(Alert::getId)
                .containsExactly(open.getId());
    }

    @Test
    void acknowledgesAnAlertOnBehalfOfAUser() {
        Alert alert = service.createAlertFromRisk(risk(UUID.randomUUID(), "MEDIUM"));
        UUID userId = UUID.randomUUID();

        Alert acknowledged = service.acknowledgeAlert(alert.getId(), userId);

        assertThat(acknowledged.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(acknowledged.getAcknowledgedBy()).isEqualTo(userId);
        assertThat(acknowledged.getAcknowledgedAt()).isEqualTo(AlertFixtures.NOW);
    }

    @Test
    void acknowledgingAnAlertTwiceFails() {
        Alert alert = service.createAlertFromRisk(risk(UUID.randomUUID(), "MEDIUM"));
        service.acknowledgeAlert(alert.getId(), null);

        assertThatThrownBy(() -> service.acknowledgeAlert(alert.getId(), null))
                .isInstanceOf(AlertStateException.class);
    }

    @Test
    void failsToGetAnUnknownAlert() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> service.getAlert(unknownId))
                .isInstanceOf(AlertNotFoundException.class)
                .hasMessage("Alert " + unknownId + " was not found");
    }

    @Test
    void configuresAndThenChangesTheThresholdOfASensor() {
        service.configureThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10);

        SensorThreshold changed = service.configureThreshold(SENSOR_ID, BUILDING_ID, 0.03, 0.06);

        assertThat(changed.getWarningValue()).isEqualTo(0.03);
        assertThat(changed.getCriticalValue()).isEqualTo(0.06);
        assertThat(service.listThresholds(BUILDING_ID)).hasSize(1);
    }

    @Test
    void aSensorKeepsTheBuildingOfItsFirstThreshold() {
        service.configureThreshold(SENSOR_ID, BUILDING_ID, 0.05, 0.10);
        UUID otherBuilding = UUID.randomUUID();

        assertThatThrownBy(() -> service.configureThreshold(SENSOR_ID, otherBuilding, 0.05, 0.10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sensor " + SENSOR_ID + " belongs to building " + BUILDING_ID);
    }

    @Test
    void failsToGetTheThresholdOfASensorWithoutOne() {
        assertThatThrownBy(() -> service.getThreshold(SENSOR_ID))
                .isInstanceOf(ThresholdNotFoundException.class)
                .hasMessage("Sensor " + SENSOR_ID + " has no threshold");
    }
}
