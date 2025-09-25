package com.ecommerce.order.repository;

import com.ecommerce.common.event.AggregateRepository;
import com.ecommerce.order.aggregate.OrderAggregate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderAggregateRepository extends AggregateRepository<OrderAggregate> {

    @Override
    protected OrderAggregate createNewAggregate() {
        return new OrderAggregate();
    }

    @Override
    protected String getAggregateType() {
        return "Order";
    }
}