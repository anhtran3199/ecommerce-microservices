package com.ecommerce.order.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
    "com.ecommerce.order.repository",
    "com.ecommerce.common.event"
})
@EntityScan(basePackages = {
    "com.ecommerce.order.entity",
    "com.ecommerce.common.event"
})
public class EventSourcingConfig {
}