package com.ecommerce.common.messaging;

import com.ecommerce.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    public void publishEvent(DomainEvent event) {
        try {
            log.info("Publishing event: {}", event);
            String routingKey = getRoutingKeyForEvent(event);

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.DOMAIN_EVENTS_EXCHANGE,
                routingKey,
                event
            );

            log.info("Successfully published event: {}", event);

        } catch (Exception e) {
            log.error("Failed to publish event: {}, will retry", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @Recover
    public void recoverFromEventPublishFailure(Exception ex, DomainEvent event) {
        log.error("Failed to publish event after all retries: {}", event, ex);

        // Store failed event for manual processing or send to DLQ
        storeFailedEvent(event, ex);
    }

    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
    )
    public void publishSagaCommand(SagaCommand command) {
        try {
            log.info("Publishing saga command: {}", command);

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAGA_COMMANDS_EXCHANGE,
                RabbitMQConfig.SAGA_COMMANDS_ROUTING_KEY,
                command
            );

            log.info("Successfully published saga command: {}", command);

        } catch (Exception e) {
            log.error("Failed to publish saga command: {}, will retry", command, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @Recover
    public void recoverFromSagaCommandPublishFailure(Exception ex, SagaCommand command) {
        log.error("Failed to publish saga command after all retries: {}", command, ex);
        storeFailedSagaCommand(command, ex);
    }

    private void storeFailedEvent(DomainEvent event, Exception ex) {
        // Store failed event in database for manual retry or send to external monitoring
        log.error("Storing failed event for manual processing: {} - Error: {}", event, ex.getMessage());

        // Could implement:
        // - Database storage
        // - File system backup
        // - External monitoring system notification
        // - Send to special DLQ for manual processing
    }

    private void storeFailedSagaCommand(SagaCommand command, Exception ex) {
        log.error("Storing failed saga command for manual processing: {} - Error: {}", command, ex.getMessage());

        // Similar to storeFailedEvent - implement based on requirements
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