package com.ecommerce.common.cqrs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public abstract class Command {

    private final String commandId;
    private final LocalDateTime timestamp;

    protected Command() {
        this.commandId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
}