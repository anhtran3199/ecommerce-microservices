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

        // Debug logging
        System.out.println("Found " + handlerBeans.size() + " CommandHandler beans:");
        for (String beanName : handlerBeans.keySet()) {
            System.out.println("  - " + beanName + ": " + handlerBeans.get(beanName).getClass().getName());
        }

        for (CommandHandler handler : handlerBeans.values()) {
            Class<?> handlerClass = handler.getClass();
            System.out.println("Checking handler: " + handlerClass.getName());

            // Check direct interfaces
            Type[] genericInterfaces = handlerClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                System.out.println("  Interface: " + genericInterface);
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0) {
                        System.out.println("    Type argument: " + typeArguments[0] + ", Looking for: " + commandType);
                        if (typeArguments[0].equals(commandType)) {
                            System.out.println("    MATCH FOUND!");
                            return handler;
                        }
                    }
                }
            }

            // Also check superclass interfaces (in case of inheritance)
            Class<?> superClass = handlerClass.getSuperclass();
            while (superClass != null && superClass != Object.class) {
                Type[] superInterfaces = superClass.getGenericInterfaces();
                for (Type genericInterface : superInterfaces) {
                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length > 0 && typeArguments[0].equals(commandType)) {
                            return handler;
                        }
                    }
                }
                superClass = superClass.getSuperclass();
            }
        }
        System.out.println("No handler found for command: " + commandType.getName());
        return null;
    }
}