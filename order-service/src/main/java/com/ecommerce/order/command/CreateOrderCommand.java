package com.ecommerce.order.command;

import com.ecommerce.common.cqrs.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateOrderCommand extends Command {

    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;

    public CreateOrderCommand(Long userId, Long productId, Integer quantity, BigDecimal totalAmount) {
        super();
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }
}