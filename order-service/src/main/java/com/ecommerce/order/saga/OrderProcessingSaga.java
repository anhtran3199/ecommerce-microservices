package com.ecommerce.order.saga;

import com.ecommerce.common.event.DomainEvent;
import com.ecommerce.common.messaging.SagaCommand;
import com.ecommerce.common.saga.Saga;
import com.ecommerce.common.saga.SagaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderProcessingSaga extends Saga {

    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;

    public OrderProcessingSaga() {
        super();
    }

    public OrderProcessingSaga(Long orderId, Long userId, Long productId, Integer quantity, BigDecimal totalAmount) {
        super();
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        setCurrentStep("ORDER_CREATED");
    }

    @Override
    public void handle(DomainEvent event) {
        switch (event.getEventType()) {
            case "OrderCreatedEvent":
                handleOrderCreated(event);
                break;
            case "StockReservedEvent":
                handleStockReserved(event);
                break;
            case "StockReservationFailedEvent":
                handleStockReservationFailed(event);
                break;
            case "PaymentProcessedEvent":
                handlePaymentProcessed(event);
                break;
            case "PaymentFailedEvent":
                handlePaymentFailed(event);
                break;
            default:
                // Ignore unknown events
                break;
        }
    }

    private void handleOrderCreated(DomainEvent event) {
        setCurrentStep("RESERVING_STOCK");
        setStatus(SagaStatus.IN_PROGRESS);

        // Command to reserve stock
        SagaCommand reserveStockCommand = new SagaCommand(
            "ReserveStockCommand",
            getSagaId(),
            "product-service",
            new ReserveStockRequest(productId, quantity, orderId)
        );

        addCommand(reserveStockCommand);
    }

    private void handleStockReserved(DomainEvent event) {
        setCurrentStep("PROCESSING_PAYMENT");

        // Command to process payment
        SagaCommand processPaymentCommand = new SagaCommand(
            "ProcessPaymentCommand",
            getSagaId(),
            "payment-service",
            new ProcessPaymentRequest(orderId, userId, totalAmount)
        );

        addCommand(processPaymentCommand);
    }

    private void handleStockReservationFailed(DomainEvent event) {
        setCurrentStep("CANCELLING_ORDER");
        markCompensating();

        // Command to cancel order
        SagaCommand cancelOrderCommand = new SagaCommand(
            "CancelOrderCommand",
            getSagaId(),
            "order-service",
            new CancelOrderRequest(orderId, "Stock not available")
        );

        addCommand(cancelOrderCommand);
        markFailed();
    }

    private void handlePaymentProcessed(DomainEvent event) {
        setCurrentStep("CONFIRMING_ORDER");

        // Command to confirm order
        SagaCommand confirmOrderCommand = new SagaCommand(
            "ConfirmOrderCommand",
            getSagaId(),
            "order-service",
            new ConfirmOrderRequest(orderId)
        );

        addCommand(confirmOrderCommand);
        markCompleted();
    }

    private void handlePaymentFailed(DomainEvent event) {
        setCurrentStep("COMPENSATING");
        markCompensating();

        // Command to release stock
        SagaCommand releaseStockCommand = new SagaCommand(
            "ReleaseStockCommand",
            getSagaId(),
            "product-service",
            new ReleaseStockRequest(productId, quantity, orderId)
        );

        addCommand(releaseStockCommand);

        // Command to cancel order
        SagaCommand cancelOrderCommand = new SagaCommand(
            "CancelOrderCommand",
            getSagaId(),
            "order-service",
            new CancelOrderRequest(orderId, "Payment failed")
        );

        addCommand(cancelOrderCommand);
        markFailed();
    }

    // Inner classes for command payloads
    @Data
    public static class ReserveStockRequest {
        private Long productId;
        private Integer quantity;
        private Long orderId;

        public ReserveStockRequest(Long productId, Integer quantity, Long orderId) {
            this.productId = productId;
            this.quantity = quantity;
            this.orderId = orderId;
        }
    }

    @Data
    public static class ProcessPaymentRequest {
        private Long orderId;
        private Long userId;
        private BigDecimal amount;

        public ProcessPaymentRequest(Long orderId, Long userId, BigDecimal amount) {
            this.orderId = orderId;
            this.userId = userId;
            this.amount = amount;
        }
    }

    @Data
    public static class CancelOrderRequest {
        private Long orderId;
        private String reason;

        public CancelOrderRequest(Long orderId, String reason) {
            this.orderId = orderId;
            this.reason = reason;
        }
    }

    @Data
    public static class ConfirmOrderRequest {
        private Long orderId;

        public ConfirmOrderRequest(Long orderId) {
            this.orderId = orderId;
        }
    }

    @Data
    public static class ReleaseStockRequest {
        private Long productId;
        private Integer quantity;
        private Long orderId;

        public ReleaseStockRequest(Long productId, Integer quantity, Long orderId) {
            this.productId = productId;
            this.quantity = quantity;
            this.orderId = orderId;
        }
    }
}