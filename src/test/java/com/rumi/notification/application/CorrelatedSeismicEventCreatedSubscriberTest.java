package com.rumi.notification.application;

import com.rumi.notification.AlertFixtures;
import com.rumi.notification.domain.model.Alert;
import com.rumi.notification.domain.model.AlertSeverity;
import com.rumi.notification.infrastructure.messaging.rabbitmq.CorrelatedSeismicEventCreated;
import com.rumi.notification.infrastructure.messaging.rabbitmq.CorrelatedSeismicEventCreatedSubscriber;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// In this package because the in-memory repositories are package-private.
class CorrelatedSeismicEventCreatedSubscriberTest {

    private static final UUID BUILDING_ID = AlertFixtures.BUILDING_ID;

    private final InMemoryAlertRepository alertRepository = new InMemoryAlertRepository();
    private final CorrelatedSeismicEventCreatedSubscriber subscriber = new CorrelatedSeismicEventCreatedSubscriber(
            new AlertApplicationService(alertRepository, new InMemorySensorThresholdRepository(),
                    Clock.fixed(AlertFixtures.NOW, ZoneOffset.UTC)));

    private CorrelatedSeismicEventCreated event(UUID riskIndexId, String level) {
        return new CorrelatedSeismicEventCreated(BUILDING_ID, riskIndexId, UUID.randomUUID(), level,
                AlertFixtures.CALCULATED_AT);
    }

    @Test
    void aValidEventCreatesAnAlertOfTheBuildingWithTheSeverityOfTheRiskLevel() {
        UUID riskIndexId = UUID.randomUUID();

        subscriber.onCorrelatedSeismicEventCreated(event(riskIndexId, "HIGH"));

        assertThat(alertRepository.saved()).hasSize(1);
        Alert alert = alertRepository.saved().get(0);
        assertThat(alert.getBuildingId()).isEqualTo(BUILDING_ID);
        assertThat(alert.getRiskIndexId()).isEqualTo(riskIndexId);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.HIGH);
    }

    @Test
    void theSameEventDeliveredTwiceLeavesOnlyOneAlert() {
        CorrelatedSeismicEventCreated event = event(UUID.randomUUID(), "MEDIUM");

        subscriber.onCorrelatedSeismicEventCreated(event);
        subscriber.onCorrelatedSeismicEventCreated(event);

        assertThat(alertRepository.saved()).hasSize(1);
    }

    @Test
    void rejectsAnUnknownRiskLevelWithoutRequeueingOrSavingAnAlert() {
        assertThatThrownBy(() -> subscriber.onCorrelatedSeismicEventCreated(event(UUID.randomUUID(), "SEVERE")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);

        assertThat(alertRepository.saved()).isEmpty();
    }

    @Test
    void rejectsAnEventWithoutRiskIndexIdWithoutRequeueingOrSavingAnAlert() {
        assertThatThrownBy(() -> subscriber.onCorrelatedSeismicEventCreated(event(null, "HIGH")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);

        assertThat(alertRepository.saved()).isEmpty();
    }
}
