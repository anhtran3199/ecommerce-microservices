package com.ecommerce.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProduct(Long id) {
        log.error("Product service unavailable for product ID: {}", id);
        throw new RuntimeException("Product service temporarily unavailable. Please try again later.");
    }

    @Override
    public ProductResponse updateStock(Long id, Integer quantity) {
        log.error("Failed to update stock for product ID: {} with quantity: {}", id, quantity);
        throw new RuntimeException("Stock update failed. Please try again later.");
    }
}