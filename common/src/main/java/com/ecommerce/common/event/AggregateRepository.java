package com.ecommerce.common.event;

import com.ecommerce.common.messaging.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AggregateRepository<T extends AggregateRoot> {

    @Autowired
    private EventStore eventStore;

    @Autowired
    private EventPublisher eventPublisher;

    public void save(T aggregate) {
        List<DomainEvent> uncommittedEvents = aggregate.getUncommittedEvents();

        if (!uncommittedEvents.isEmpty()) {
            // Save events to event store
            eventStore.saveEvents(
                aggregate.getId(),
                uncommittedEvents,
                aggregate.getVersion() - uncommittedEvents.size()
            );

            // Publish events to message queue
            uncommittedEvents.forEach(eventPublisher::publishEvent);

            // Mark events as committed
            aggregate.markEventsAsCommitted();
        }
    }

    public T findById(String id) {
        List<DomainEvent> events = eventStore.getEventsForAggregate(id);

        if (events.isEmpty()) {
            return null;
        }

        T aggregate = createNewAggregate();
        aggregate.replayEvents(events);
        return aggregate;
    }

    protected abstract T createNewAggregate();

    protected abstract String getAggregateType();
}