package com.ecommerce.payment.service;

import com.ecommerce.payment.client.OrderClient;
import com.ecommerce.payment.client.OrderResponse;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderClient orderClient;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Payment processPayment(Payment payment) {
        OrderResponse order = orderClient.getOrder(payment.getOrderId());

        if (order == null) {
            throw new RuntimeException("Order not found: " + payment.getOrderId());
        }

        if (!order.getTotalAmount().equals(payment.getAmount())) {
            throw new RuntimeException("Payment amount does not match order total");
        }

        payment.setUserId(order.getUserId());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionId(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);

        try {
            boolean paymentSuccess = simulatePaymentProcessing();

            if (paymentSuccess) {
                savedPayment.setStatus(PaymentStatus.COMPLETED);
                orderClient.updateOrderStatus(payment.getOrderId(), "CONFIRMED");
            } else {
                savedPayment.setStatus(PaymentStatus.FAILED);
                orderClient.updateOrderStatus(payment.getOrderId(), "CANCELLED");
            }

            return paymentRepository.save(savedPayment);

        } catch (Exception e) {
            savedPayment.setStatus(PaymentStatus.FAILED);
            orderClient.updateOrderStatus(payment.getOrderId(), "CANCELLED");
            return paymentRepository.save(savedPayment);
        }
    }

    private boolean simulatePaymentProcessing() {
        try {
            Thread.sleep(2000);
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public Payment updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        try {
            payment.setStatus(Enum.valueOf(PaymentStatus.class, status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + status);
        }

        return paymentRepository.save(payment);
    }
}