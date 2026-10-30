package com.rumi.notification.infrastructure.persistence;

import com.rumi.notification.AlertFixtures;
import com.rumi.notification.domain.model.SensorThreshold;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaSensorThresholdRepository.class)
class JpaSensorThresholdRepositoryTest {

    @Autowired
    private JpaSensorThresholdRepository thresholdRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAndChangesTheThresholdOfASensor() {
        SensorThreshold threshold = thresholdRepository.save(AlertFixtures.threshold());
        entityManager.flush();
        entityManager.clear();

        threshold.changeLimits(0.03, 0.06, AlertFixtures.NOW.plusSeconds(60));
        thresholdRepository.save(threshold);
        entityManager.flush();
        entityManager.clear();

        SensorThreshold stored = thresholdRepository.findBySensorId(AlertFixtures.SENSOR_ID).orElseThrow();
        assertThat(stored.getBuildingId()).isEqualTo(AlertFixtures.BUILDING_ID);
        assertThat(stored.getWarningValue()).isEqualTo(0.03);
        assertThat(stored.getCriticalValue()).isEqualTo(0.06);
        assertThat(stored.getUpdatedAt()).isEqualTo(AlertFixtures.NOW.plusSeconds(60));
    }

    @Test
    void listsTheThresholdsOfABuildingOnly() {
        UUID buildingId = UUID.randomUUID();
        thresholdRepository.save(new SensorThreshold(UUID.randomUUID(), buildingId, 0.05, 0.10, AlertFixtures.NOW));
        thresholdRepository.save(new SensorThreshold(UUID.randomUUID(), buildingId, 0.02, 0.04, AlertFixtures.NOW));
        thresholdRepository.save(AlertFixtures.threshold());
        entityManager.flush();
        entityManager.clear();

        assertThat(thresholdRepository.findByBuildingId(buildingId)).hasSize(2)
                .allSatisfy(threshold -> assertThat(threshold.getBuildingId()).isEqualTo(buildingId));
    }

    @Test
    void findsNothingForASensorWithoutThreshold() {
        assertThat(thresholdRepository.findBySensorId(UUID.randomUUID())).isEmpty();
    }
}
