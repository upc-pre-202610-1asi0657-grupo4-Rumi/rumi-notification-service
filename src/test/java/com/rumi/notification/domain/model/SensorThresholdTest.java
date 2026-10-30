package com.rumi.notification.domain.model;

import com.rumi.notification.AlertFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorThresholdTest {

    private final SensorThreshold threshold = AlertFixtures.threshold();

    @Test
    void aReadingBelowTheWarningValueRaisesNoAlert() {
        assertThat(threshold.classify(0.049)).isEmpty();
    }

    @Test
    void aReadingAtTheWarningValueRaisesAHighAlert() {
        assertThat(threshold.classify(0.05)).contains(AlertSeverity.HIGH);
    }

    @Test
    void aReadingAtTheCriticalValueRaisesACriticalAlert() {
        assertThat(threshold.classify(0.10)).contains(AlertSeverity.CRITICAL);
        assertThat(threshold.classify(0.30)).contains(AlertSeverity.CRITICAL);
    }

    @Test
    void returnsTheLimitThatEachSeverityExceeded() {
        assertThat(threshold.limitFor(AlertSeverity.HIGH)).isEqualTo(0.05);
        assertThat(threshold.limitFor(AlertSeverity.CRITICAL)).isEqualTo(0.10);
    }

    @Test
    void theWarningValueMustBeLowerThanTheCriticalValue() {
        assertThatThrownBy(() -> new SensorThreshold(
                AlertFixtures.SENSOR_ID, AlertFixtures.BUILDING_ID, 0.10, 0.10, AlertFixtures.NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warning value must be lower than the critical value");
    }

    @Test
    void theWarningValueMustBePositive() {
        assertThatThrownBy(() -> new SensorThreshold(
                AlertFixtures.SENSOR_ID, AlertFixtures.BUILDING_ID, 0.0, 0.10, AlertFixtures.NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Warning value must be greater than 0");
    }

    @Test
    void changesItsLimitsAndTheUpdateTime() {
        Instant changedAt = AlertFixtures.NOW.plusSeconds(60);

        threshold.changeLimits(0.02, 0.08, changedAt);

        assertThat(threshold.getWarningValue()).isEqualTo(0.02);
        assertThat(threshold.getCriticalValue()).isEqualTo(0.08);
        assertThat(threshold.getUpdatedAt()).isEqualTo(changedAt);
    }

    @Test
    void rejectsInvalidNewLimitsAndKeepsTheOldOnes() {
        assertThatThrownBy(() -> threshold.changeLimits(0.20, 0.10, AlertFixtures.NOW))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(threshold.getWarningValue()).isEqualTo(0.05);
        assertThat(threshold.getCriticalValue()).isEqualTo(0.10);
    }

    @Test
    void rejectsAReadingThatIsNotANumber() {
        assertThatThrownBy(() -> threshold.classify(Double.NaN)).isInstanceOf(IllegalArgumentException.class);
    }
}
