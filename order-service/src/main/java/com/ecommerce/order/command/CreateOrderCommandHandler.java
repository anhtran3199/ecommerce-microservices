package com.ecommerce.order.command;

import com.ecommerce.common.cqrs.CommandHandler;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.ProductResponse;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductClient productClient;

    @Override
    public void handle(CreateOrderCommand command) {
        // Create order entity with multiple OrderItems
        Order order = new Order();
        order.setUserId(command.getUserId());
        order.setStatus(OrderStatus.PENDING);

        // Calculate total amount and create OrderItems
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderRequest request : command.getOrderItems()) {
            // Get product info from product service
            ProductResponse product = productClient.getProduct(request.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found: " + request.getProductId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(request.getProductId());
            orderItem.setQuantity(request.getQuantity());
            orderItem.setPrice(product.getPrice()); // Get price from product service
            orderItem.setOrder(order);

            orderItems.add(orderItem);
            // Calculate total: price * quantity
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        orderService.saveOrder(order);
    }
}