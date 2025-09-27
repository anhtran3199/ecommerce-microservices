package com.ecommerce.order.listener;

import com.ecommerce.common.messaging.RabbitMQConfig;
import com.ecommerce.common.saga.SagaManager;
import com.ecommerce.order.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    private SagaManager sagaManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.ORDER_EVENTS_QUEUE)
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 1.5, maxDelay = 30000)
    )
    public void handleOrderEvent(OrderCreatedEvent event) {
        try {
            log.info("Processing order event: {}", event);

            // Process order events if needed
            sagaManager.handleEvent(event);

            log.info("Successfully processed order event: {}", event);

        } catch (Exception e) {
            log.error("Error processing order event: {}, attempt will be retried", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_EVENTS_DLQ)
    public void handleOrderEventDLQ(OrderCreatedEvent event) {
        log.error("Order event processing failed permanently, sending to DLQ: {}", event);
        // Handle dead letter - send notification, store for manual processing, etc.
    }
}