package com.ecommerce.order.event;

import com.ecommerce.common.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderCancelledEvent extends DomainEvent {

    private Long orderId;
    private Long userId;
    private String reason;
    private String status;

    public OrderCancelledEvent(Long orderId, Long userId, String reason, String status, Long version) {
        super(orderId.toString(), "Order", version);
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
        this.status = status;
    }
}