package com.ecommerce.common.event;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {

    private String id;
    private Long version = 0L;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    protected AggregateRoot() {}

    protected AggregateRoot(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }

    protected void applyEvent(DomainEvent event) {
        applyEvent(event, true);
    }

    private void applyEvent(DomainEvent event, boolean isNew) {
        handleEvent(event);

        if (isNew) {
            uncommittedEvents.add(event);
        }

        version++;
    }

    public void replayEvents(List<DomainEvent> events) {
        events.forEach(event -> applyEvent(event, false));
    }

    protected abstract void handleEvent(DomainEvent event);

    protected void setId(String id) {
        this.id = id;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }
}