package com.rumi.notification.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelatedSeismicEventCreatedContractTest {

    private static final String PAYLOAD = """
            {
              "buildingId": "7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d",
              "riskIndexId": "3f2b8c1e-6d4a-4e7b-9a10-5c8d2e1f7b42",
              "seismicEventId": "9e4d7a20-1b3c-4f58-8d6e-2a7c9b0e4f13",
              "riskLevel": "HIGH",
              "calculatedAt": "2026-10-30T15:30:00Z",
              "modelVersion": "v1",
              "affectedZones": ["FLOOR-3-NORTH"]
            }""";

    @Test
    void readsTheEventIntoItsOwnContractClassIgnoringTheExtraFields() {
        MessageConverter converter = new NotificationMessagingConfiguration()
                .notificationMessageConverter(JsonMapper.builder().findAndAddModules().build());
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        // What the listener container infers from the @RabbitListener method parameter.
        properties.setInferredArgumentType(CorrelatedSeismicEventCreated.class);

        Object event = converter.fromMessage(new Message(PAYLOAD.getBytes(StandardCharsets.UTF_8), properties));

        assertThat(event).isEqualTo(new CorrelatedSeismicEventCreated(
                UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d"),
                UUID.fromString("3f2b8c1e-6d4a-4e7b-9a10-5c8d2e1f7b42"),
                UUID.fromString("9e4d7a20-1b3c-4f58-8d6e-2a7c9b0e4f13"),
                "HIGH",
                Instant.parse("2026-10-30T15:30:00Z")
        ));
    }
}
