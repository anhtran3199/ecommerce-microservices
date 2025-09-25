package com.ecommerce.order.aggregate;

import com.ecommerce.common.event.AggregateRoot;
import com.ecommerce.common.event.DomainEvent;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.event.OrderCancelledEvent;
import com.ecommerce.order.event.OrderConfirmedEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderAggregate extends AggregateRoot {

    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String cancellationReason;

    public OrderAggregate() {
        super();
    }

    public OrderAggregate(String aggregateId) {
        super(aggregateId);
    }

    public static OrderAggregate createOrder(Long orderId, Long userId, Long productId,
                                           Integer quantity, BigDecimal totalAmount) {
        OrderAggregate aggregate = new OrderAggregate(orderId.toString());

        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId, userId, productId, quantity, totalAmount,
            OrderStatus.PENDING.name(), aggregate.getVersion() + 1
        );

        aggregate.applyEvent(event);
        return aggregate;
    }

    public void confirmOrder() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be confirmed when in PENDING status");
        }

        OrderConfirmedEvent event = new OrderConfirmedEvent(
            orderId, userId, OrderStatus.CONFIRMED.name(), getVersion() + 1
        );

        applyEvent(event);
    }

    public void cancelOrder(String reason) {
        if (status == OrderStatus.CANCELLED) {
            return; // Already cancelled
        }

        OrderCancelledEvent event = new OrderCancelledEvent(
            orderId, userId, reason, OrderStatus.CANCELLED.name(), getVersion() + 1
        );

        applyEvent(event);
    }

    @Override
    protected void handleEvent(DomainEvent event) {
        switch (event.getEventType()) {
            case "OrderCreatedEvent":
                handle((OrderCreatedEvent) event);
                break;
            case "OrderConfirmedEvent":
                handle((OrderConfirmedEvent) event);
                break;
            case "OrderCancelledEvent":
                handle((OrderCancelledEvent) event);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }

    private void handle(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.userId = event.getUserId();
        this.productId = event.getProductId();
        this.quantity = event.getQuantity();
        this.totalAmount = event.getTotalAmount();
        this.status = OrderStatus.valueOf(event.getStatus());
        setId(event.getOrderId().toString());
    }

    private void handle(OrderConfirmedEvent event) {
        this.status = OrderStatus.valueOf(event.getStatus());
    }

    private void handle(OrderCancelledEvent event) {
        this.status = OrderStatus.valueOf(event.getStatus());
        this.cancellationReason = event.getReason();
    }
}