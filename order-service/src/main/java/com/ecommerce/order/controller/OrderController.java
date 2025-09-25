package com.ecommerce.order.controller;

import com.ecommerce.common.cqrs.CommandBus;
import com.ecommerce.common.cqrs.QueryBus;
import com.ecommerce.common.util.SecurityUtil;
import com.ecommerce.order.command.CreateOrderCommand;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommandBus commandBus;

	@Autowired
	private QueryBus queryBus;

	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {
		List<Order> orders = orderService.getAllOrders();
		return ResponseEntity.ok(orders);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
		Optional<Order> order = orderService.getOrderById(id);
		return order.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
		List<Order> orders = orderService.getOrdersByUserId(userId);
		return ResponseEntity.ok(orders);
	}

	@PostMapping
	public ResponseEntity<?> createOrder(@Valid @RequestBody List<CreateOrderRequest> requests) {
		try {
			CreateOrderCommand command = new CreateOrderCommand(
					SecurityUtil.getCurrentUserId(),
					requests
			);

			commandBus.send(command);

			return ResponseEntity.status(HttpStatus.ACCEPTED)
					.body("Order creation initiated");
		}
		catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<?> updateOrderStatus(@PathVariable("id") Long id, @RequestParam String status) {
		try {
			Order updatedOrder = orderService.updateOrderStatus(id, status);
			return ResponseEntity.ok(updatedOrder);
		}
		catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteOrder(@PathVariable("id") Long id) {
		try {
			orderService.deleteOrder(id);
			return ResponseEntity.ok().build();
		}
		catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}