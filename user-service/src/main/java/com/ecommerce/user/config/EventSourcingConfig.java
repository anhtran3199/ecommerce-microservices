package com.ecommerce.user.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
	basePackages = {
			"com.ecommerce.user.repository",
			"com.ecommerce.common.event"
	},
	includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
			type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
			classes = {com.ecommerce.common.event.EventStoreRepository.class}
	)
)
@EntityScan(basePackages = {
	"com.ecommerce.user.entity",
	"com.ecommerce.common.event"
})
public class EventSourcingConfig {
}