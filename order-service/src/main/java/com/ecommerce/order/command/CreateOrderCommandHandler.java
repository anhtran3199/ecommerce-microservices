package com.ecommerce.order.command;

import com.ecommerce.common.cqrs.CommandHandler;
import com.ecommerce.common.saga.SagaManager;
import com.ecommerce.order.aggregate.OrderAggregate;
import com.ecommerce.order.repository.OrderAggregateRepository;
import com.ecommerce.order.saga.OrderProcessingSaga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {

    @Autowired
    private OrderAggregateRepository orderRepository;

    @Autowired
    private SagaManager sagaManager;

    @Override
    public void handle(CreateOrderCommand command) {
        // Generate order ID (in real scenario, this could be from a sequence or UUID)
        Long orderId = System.currentTimeMillis(); // Simplified ID generation

        // Create order aggregate
        OrderAggregate orderAggregate = OrderAggregate.createOrder(
            orderId,
            command.getUserId(),
            command.getProductId(),
            command.getQuantity(),
            command.getTotalAmount()
        );

        // Save order aggregate (this will publish OrderCreatedEvent)
        orderRepository.save(orderAggregate);

        // Start order processing saga
        OrderProcessingSaga saga = new OrderProcessingSaga(
            orderId,
            command.getUserId(),
            command.getProductId(),
            command.getQuantity(),
            command.getTotalAmount()
        );

        sagaManager.startSaga(saga);
    }
}