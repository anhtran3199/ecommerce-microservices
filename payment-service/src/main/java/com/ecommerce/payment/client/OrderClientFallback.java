package com.ecommerce.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderResponse getOrder(Long id) {
        log.error("Order service unavailable for order ID: {}", id);
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.");
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        log.error("Failed to update order status for order ID: {} to status: {}", id, status);
        throw new RuntimeException("Order status update failed. Please try again later.");
    }
}