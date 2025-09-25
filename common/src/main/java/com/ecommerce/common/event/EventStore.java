package com.ecommerce.common.event;

import java.util.List;

public interface EventStore {

    void saveEvents(String aggregateId, List<DomainEvent> events, Long expectedVersion);

    List<DomainEvent> getEventsForAggregate(String aggregateId);

    List<DomainEvent> getEventsForAggregateFromVersion(String aggregateId, Long version);

    List<DomainEvent> getAllEvents();

    List<DomainEvent> getEventsByType(String eventType);
}