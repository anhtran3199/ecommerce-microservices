package com.ecommerce.common.cqrs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Service
public class QueryBus {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<Class<?>, QueryHandler> handlers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <Q extends Query<R>, R> R send(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) getHandler((Class<Q>) query.getClass());
        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler found for query: " + query.getClass().getSimpleName()
            );
        }
        return handler.handle(query);
    }

    @SuppressWarnings("unchecked")
    private <Q extends Query<R>, R> QueryHandler<Q, R> getHandler(Class<Q> queryType) {
        if (!handlers.containsKey(queryType)) {
            QueryHandler<Q, R> handler = findHandler(queryType);
            if (handler != null) {
                handlers.put(queryType, handler);
            }
        }
        return (QueryHandler<Q, R>) handlers.get(queryType);
    }

    @SuppressWarnings("unchecked")
    private <Q extends Query<R>, R> QueryHandler<Q, R> findHandler(Class<Q> queryType) {
        Map<String, QueryHandler> handlerBeans = applicationContext.getBeansOfType(QueryHandler.class);

        for (QueryHandler handler : handlerBeans.values()) {
            Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0].equals(queryType)) {
                        return handler;
                    }
                }
            }
        }
        return null;
    }
}