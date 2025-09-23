package com.ecommerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    OrderResponse getOrder(@PathVariable("id") Long id);

    @PutMapping("/api/orders/{id}/status")
    OrderResponse updateOrderStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}