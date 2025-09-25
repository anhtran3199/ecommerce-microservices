package com.ecommerce.common.cqrs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommandBus {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<Class<?>, CommandHandler> handlers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Command> void send(T command) {
        CommandHandler<T> handler = (CommandHandler<T>) getHandler((Class<T>) command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler found for command: " + command.getClass().getSimpleName()
            );
        }
        handler.handle(command);
    }

    @SuppressWarnings("unchecked")
    private <T extends Command> CommandHandler<T> getHandler(Class<T> commandType) {
        if (!handlers.containsKey(commandType)) {
            CommandHandler<T> handler = findHandler(commandType);
            if (handler != null) {
                handlers.put(commandType, handler);
            }
        }
        return (CommandHandler<T>) handlers.get(commandType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Command> CommandHandler<T> findHandler(Class<T> commandType) {
        Map<String, CommandHandler> handlerBeans = applicationContext.getBeansOfType(CommandHandler.class);

        for (CommandHandler handler : handlerBeans.values()) {
            Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0].equals(commandType)) {
                        return handler;
                    }
                }
            }
        }
        return null;
    }
}