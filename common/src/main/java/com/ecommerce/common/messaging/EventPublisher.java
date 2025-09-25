package com.ecommerce.common.messaging;

import com.ecommerce.common.event.DomainEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishEvent(DomainEvent event) {
        String routingKey = getRoutingKeyForEvent(event);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.DOMAIN_EVENTS_EXCHANGE,
            routingKey,
            event
        );
    }

    public void publishSagaCommand(SagaCommand command) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.SAGA_COMMANDS_EXCHANGE,
            RabbitMQConfig.SAGA_COMMANDS_ROUTING_KEY,
            command
        );
    }

    private String getRoutingKeyForEvent(DomainEvent event) {
        String eventType = event.getEventType();

        if (eventType.contains("Order")) {
            return RabbitMQConfig.ORDER_EVENTS_ROUTING_KEY;
        } else if (eventType.contains("Product") || eventType.contains("Stock")) {
            return RabbitMQConfig.PRODUCT_EVENTS_ROUTING_KEY;
        } else if (eventType.contains("Payment")) {
            return RabbitMQConfig.PAYMENT_EVENTS_ROUTING_KEY;
        }

        return "default.events";
    }
}