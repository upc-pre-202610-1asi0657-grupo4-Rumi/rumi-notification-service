package com.rumi.notification.infrastructure.messaging.rabbitmq;

import com.rumi.notification.application.AlertApplicationService;
import com.rumi.notification.application.CreateAlertFromRiskCommand;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CorrelatedSeismicEventCreatedSubscriber {

    private final AlertApplicationService alertService;

    public CorrelatedSeismicEventCreatedSubscriber(AlertApplicationService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(queues = NotificationMessagingConfiguration.QUEUE)
    public void onCorrelatedSeismicEventCreated(CorrelatedSeismicEventCreated event) {
        try {
            alertService.createAlertFromRisk(new CreateAlertFromRiskCommand(
                    event.buildingId(),
                    event.riskIndexId(),
                    event.seismicEventId(),
                    event.riskLevel(),
                    event.calculatedAt()
            ));
        } catch (IllegalArgumentException exception) {
            throw new AmqpRejectAndDontRequeueException(exception.getMessage(), exception);
        }
    }
}
