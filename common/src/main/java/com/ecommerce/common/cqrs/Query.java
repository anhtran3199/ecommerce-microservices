package com.ecommerce.common.cqrs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public abstract class Query<T> {

    private final String queryId;
    private final LocalDateTime timestamp;

    protected Query() {
        this.queryId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
}