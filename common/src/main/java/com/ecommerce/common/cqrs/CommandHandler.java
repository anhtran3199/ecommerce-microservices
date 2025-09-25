package com.ecommerce.common.cqrs;

public interface CommandHandler<T extends Command> {
    void handle(T command);
}