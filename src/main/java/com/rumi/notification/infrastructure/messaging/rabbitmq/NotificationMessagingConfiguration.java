package com.rumi.notification.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class NotificationMessagingConfiguration {

    public static final String EXCHANGE = "rumi.seismic-correlation.events";
    public static final String QUEUE = "rumi.notification.correlated-seismic-event-created";
    public static final String ROUTING_KEY = "seismic-correlation.event.correlated";

    @Bean
    public TopicExchange notificationCorrelatedSeismicEventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationCorrelatedSeismicEventQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding notificationCorrelatedSeismicEventBinding(
            Queue notificationCorrelatedSeismicEventQueue,
            TopicExchange notificationCorrelatedSeismicEventExchange
    ) {
        return BindingBuilder.bind(notificationCorrelatedSeismicEventQueue)
                .to(notificationCorrelatedSeismicEventExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter notificationMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
