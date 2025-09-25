package com.ecommerce.common.saga;

import com.ecommerce.common.event.DomainEvent;
import com.ecommerce.common.messaging.SagaCommand;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public abstract class Saga {

    private String sagaId;
    private SagaStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String currentStep;
    private List<SagaCommand> pendingCommands = new ArrayList<>();
    private List<DomainEvent> processedEvents = new ArrayList<>();

    protected Saga() {
        this.sagaId = UUID.randomUUID().toString();
        this.status = SagaStatus.STARTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected Saga(String sagaId) {
        this.sagaId = sagaId;
        this.status = SagaStatus.STARTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public abstract void handle(DomainEvent event);

    protected void addCommand(SagaCommand command) {
        pendingCommands.add(command);
        updatedAt = LocalDateTime.now();
    }

    protected void markCompleted() {
        status = SagaStatus.COMPLETED;
        updatedAt = LocalDateTime.now();
    }

    protected void markFailed() {
        status = SagaStatus.FAILED;
        updatedAt = LocalDateTime.now();
    }

    protected void markCompensating() {
        status = SagaStatus.COMPENSATING;
        updatedAt = LocalDateTime.now();
    }

    protected void setCurrentStep(String step) {
        this.currentStep = step;
        updatedAt = LocalDateTime.now();
    }

    public List<SagaCommand> getAndClearPendingCommands() {
        List<SagaCommand> commands = new ArrayList<>(pendingCommands);
        pendingCommands.clear();
        return commands;
    }

    public void addProcessedEvent(DomainEvent event) {
        processedEvents.add(event);
        updatedAt = LocalDateTime.now();
    }

    public boolean hasProcessedEvent(String eventId) {
        return processedEvents.stream()
            .anyMatch(event -> event.getEventId().equals(eventId));
    }
}