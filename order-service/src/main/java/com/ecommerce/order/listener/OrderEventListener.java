package com.ecommerce.order.listener;

import com.ecommerce.common.saga.SagaManager;
import com.ecommerce.order.event.OrderCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private SagaManager sagaManager;

    @RabbitListener(queues = "order.events.queue")
    public void handleOrderEvent(OrderCreatedEvent event) {
        // Process order events if needed
        sagaManager.handleEvent(event);
    }
}