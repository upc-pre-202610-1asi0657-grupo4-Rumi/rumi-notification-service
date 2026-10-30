package com.rumi.notification.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertSeverityTest {

    @Test
    void mapsEveryRiskLevelOfSeismicCorrelationToTheSeverityWithTheSameName() {
        assertThat(AlertSeverity.fromRiskLevel("LOW")).isEqualTo(AlertSeverity.LOW);
        assertThat(AlertSeverity.fromRiskLevel("MEDIUM")).isEqualTo(AlertSeverity.MEDIUM);
        assertThat(AlertSeverity.fromRiskLevel("HIGH")).isEqualTo(AlertSeverity.HIGH);
        assertThat(AlertSeverity.fromRiskLevel("CRITICAL")).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void acceptsARiskLevelInLowerCaseOrWithSpaces() {
        assertThat(AlertSeverity.fromRiskLevel(" critical ")).isEqualTo(AlertSeverity.CRITICAL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "SEVERE"})
    void rejectsAMissingOrUnknownRiskLevel(String riskLevel) {
        assertThatThrownBy(() -> AlertSeverity.fromRiskLevel(riskLevel))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
