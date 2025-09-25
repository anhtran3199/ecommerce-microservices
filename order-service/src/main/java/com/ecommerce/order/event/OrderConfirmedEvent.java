package com.ecommerce.order.event;

import com.ecommerce.common.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderConfirmedEvent extends DomainEvent {

    private Long orderId;
    private Long userId;
    private String status;

    public OrderConfirmedEvent(Long orderId, Long userId, String status, Long version) {
        super(orderId.toString(), "Order", version);
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
    }
}