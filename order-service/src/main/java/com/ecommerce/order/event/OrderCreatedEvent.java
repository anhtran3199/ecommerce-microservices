package com.ecommerce.order.event;

import com.ecommerce.common.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderCreatedEvent extends DomainEvent {

    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;

    public OrderCreatedEvent(Long orderId, Long userId, Long productId, Integer quantity,
                           BigDecimal totalAmount, String status, Long version) {
        super(orderId.toString(), "Order", version);
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}