package com.rumi.notification.infrastructure.persistence;

import com.rumi.notification.AlertFixtures;
import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.domain.model.AlertStatus;
import com.rumi.notification.domain.model.AlertType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaAlertRepository.class)
class JpaAlertRepositoryTest {

    @Autowired
    private JpaAlertRepository alertRepository;

    @Autowired
    private TestEntityManager entityManager;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void savesAndReadsBackASeismicAlert() {
        Alert alert = AlertFixtures.seismicAlert(AlertSeverity.CRITICAL);

        alertRepository.save(alert);
        flushAndClear();

        Alert stored = alertRepository.findById(alert.getId()).orElseThrow();
        assertThat(stored.getType()).isEqualTo(AlertType.SEISMIC_RISK);
        assertThat(stored.getSeverity()).isEqualTo(AlertSeverity.CRITICAL);
        assertThat(stored.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(stored.isEvacuationRequired()).isTrue();
        assertThat(stored.getRiskIndexId()).isEqualTo(alert.getRiskIndexId());
        assertThat(stored.getOccurredAt()).isEqualTo(AlertFixtures.CALCULATED_AT);
        assertThat(stored.getCreatedAt()).isEqualTo(AlertFixtures.NOW);
    }

    @Test
    void savesTheSensorReadingOfAThresholdAlert() {
        Alert alert = AlertFixtures.thresholdAlert();

        alertRepository.save(alert);
        flushAndClear();

        Alert stored = alertRepository.findById(alert.getId()).orElseThrow();
        assertThat(stored.getSensorId()).isEqualTo(AlertFixtures.SENSOR_ID);
        assertThat(stored.getZone()).isEqualTo("FLOOR-3-NORTH");
        assertThat(stored.getMeasuredValue()).isEqualTo(0.12);
        assertThat(stored.getRiskIndexId()).isNull();
    }

    @Test
    void storesTheAcknowledgementOfAnAlert() {
        Alert alert = alertRepository.save(AlertFixtures.seismicAlert(AlertSeverity.HIGH));
        UUID userId = UUID.randomUUID();
        alert.acknowledge(userId, AlertFixtures.NOW.plusSeconds(60));

        alertRepository.save(alert);
        flushAndClear();

        Alert stored = alertRepository.findById(alert.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(stored.getAcknowledgedBy()).isEqualTo(userId);
        assertThat(stored.getAcknowledgedAt()).isEqualTo(AlertFixtures.NOW.plusSeconds(60));
    }

    @Test
    void listsTheAlertsOfABuildingNewestFirst() {
        UUID buildingId = UUID.randomUUID();
        Alert older = alertRepository.save(
                AlertFixtures.seismicAlert(buildingId, AlertSeverity.LOW, Instant.parse("2026-10-01T10:00:00Z")));
        Alert newer = alertRepository.save(
                AlertFixtures.seismicAlert(buildingId, AlertSeverity.HIGH, Instant.parse("2026-10-02T10:00:00Z")));
        alertRepository.save(AlertFixtures.seismicAlert(AlertSeverity.HIGH));
        flushAndClear();

        assertThat(alertRepository.findByBuildingId(buildingId, null))
                .extracting(Alert::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    void filtersTheAlertsOfABuildingByStatus() {
        UUID buildingId = UUID.randomUUID();
        Alert open = alertRepository.save(AlertFixtures.seismicAlert(buildingId, AlertSeverity.LOW, AlertFixtures.NOW));
        Alert resolved = AlertFixtures.seismicAlert(buildingId, AlertSeverity.LOW, AlertFixtures.NOW);
        resolved.resolve(AlertFixtures.NOW);
        alertRepository.save(resolved);
        flushAndClear();

        assertThat(alertRepository.findByBuildingId(buildingId, AlertStatus.OPEN))
                .extracting(Alert::getId)
                .containsExactly(open.getId());
    }

    @Test
    void findsTheAlertOfARiskIndex() {
        Alert alert = alertRepository.save(AlertFixtures.seismicAlert(AlertSeverity.MEDIUM));
        flushAndClear();

        assertThat(alertRepository.findByRiskIndexId(alert.getRiskIndexId()))
                .map(Alert::getId)
                .contains(alert.getId());
        assertThat(alertRepository.findByRiskIndexId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void onlyAnUnresolvedThresholdAlertCountsAsActive() {
        Alert alert = alertRepository.save(AlertFixtures.thresholdAlert());
        flushAndClear();
        assertThat(alertRepository.existsActiveThresholdAlert(AlertFixtures.SENSOR_ID)).isTrue();

        alert.resolve(AlertFixtures.NOW);
        alertRepository.save(alert);
        flushAndClear();

        assertThat(alertRepository.existsActiveThresholdAlert(AlertFixtures.SENSOR_ID)).isFalse();
    }
}
