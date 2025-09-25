package com.ecommerce.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventStoreImpl implements EventStore {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void saveEvents(String aggregateId, List<DomainEvent> events, Long expectedVersion) {
        Long currentVersion = eventStoreRepository.countByAggregateId(aggregateId);

        if (!currentVersion.equals(expectedVersion)) {
            throw new ConcurrentModificationException(
                String.format("Expected version %d but was %d for aggregate %s",
                    expectedVersion, currentVersion, aggregateId));
        }

        for (int i = 0; i < events.size(); i++) {
            DomainEvent event = events.get(i);
            event.setVersion(expectedVersion + i + 1);

            try {
                String eventData = objectMapper.writeValueAsString(event);
                EventStoreEntry entry = new EventStoreEntry();
                entry.setEventId(event.getEventId());
                entry.setEventType(event.getEventType());
                entry.setAggregateId(event.getAggregateId());
                entry.setAggregateType(event.getAggregateType());
                entry.setVersion(event.getVersion());
                entry.setEventData(eventData);
                entry.setEntityVersion(0L);
                // BaseEntity audit fields will be set automatically by JPA auditing
                eventStoreRepository.save(entry);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize event", e);
            }
        }
    }

    @Override
    public List<DomainEvent> getEventsForAggregate(String aggregateId) {
        return eventStoreRepository.findByAggregateIdOrderByVersionAsc(aggregateId)
            .stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getEventsForAggregateFromVersion(String aggregateId, Long version) {
        return eventStoreRepository.findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(aggregateId, version)
            .stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getAllEvents() {
        return eventStoreRepository.findAllByOrderByCreatedAtAsc()
            .stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getEventsByType(String eventType) {
        return eventStoreRepository.findByEventTypeOrderByCreatedAtAsc(eventType)
            .stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    private DomainEvent deserializeEvent(EventStoreEntry entry) {
        try {
            Class<?> eventClass = Class.forName(getEventClassName(entry.getEventType()));
            return (DomainEvent) objectMapper.readValue(entry.getEventData(), eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }

    private String getEventClassName(String eventType) {
        // Map event types to their full class names
        // This should be configurable or use a registry
        switch (eventType) {
            case "OrderCreatedEvent":
                return "com.ecommerce.order.event.OrderCreatedEvent";
            case "OrderConfirmedEvent":
                return "com.ecommerce.order.event.OrderConfirmedEvent";
            case "PaymentProcessedEvent":
                return "com.ecommerce.payment.event.PaymentProcessedEvent";
            case "StockReservedEvent":
                return "com.ecommerce.product.event.StockReservedEvent";
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }

    public static class ConcurrentModificationException extends RuntimeException {
        public ConcurrentModificationException(String message) {
            super(message);
        }
    }
}