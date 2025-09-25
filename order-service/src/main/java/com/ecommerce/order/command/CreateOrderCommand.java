package com.ecommerce.order.command;

import com.ecommerce.common.cqrs.Command;
import com.ecommerce.order.dto.CreateOrderRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateOrderCommand extends Command {

    private Long userId;
    private List<CreateOrderRequest> orderItems;

    public CreateOrderCommand(Long userId, List<CreateOrderRequest> orderItems) {
        super();
        this.userId = userId;
        this.orderItems = orderItems;
    }
}