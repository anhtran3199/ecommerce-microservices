package com.ecommerce.common.saga;

import com.ecommerce.common.event.DomainEvent;
import com.ecommerce.common.messaging.EventPublisher;
import com.ecommerce.common.messaging.SagaCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SagaManager {

    @Autowired
    private EventPublisher eventPublisher;

    private final Map<String, Saga> activeSagas = new ConcurrentHashMap<>();

    public void startSaga(Saga saga) {
        activeSagas.put(saga.getSagaId(), saga);
        processCommands(saga);
    }

    public void handleEvent(DomainEvent event) {
        // Find sagas that should handle this event
        activeSagas.values().stream()
            .filter(saga -> shouldHandleEvent(saga, event))
            .forEach(saga -> {
                if (!saga.hasProcessedEvent(event.getEventId())) {
                    saga.handle(event);
                    saga.addProcessedEvent(event);
                    processCommands(saga);

                    // Remove completed or failed sagas
                    if (saga.getStatus() == SagaStatus.COMPLETED ||
                        saga.getStatus() == SagaStatus.COMPENSATED) {
                        activeSagas.remove(saga.getSagaId());
                    }
                }
            });
    }

    private void processCommands(Saga saga) {
        List<SagaCommand> commands = saga.getAndClearPendingCommands();
        commands.forEach(eventPublisher::publishSagaCommand);
    }

    private boolean shouldHandleEvent(Saga saga, DomainEvent event) {
        // Simple implementation - can be made more sophisticated
        return saga.getStatus() == SagaStatus.STARTED ||
               saga.getStatus() == SagaStatus.IN_PROGRESS ||
               saga.getStatus() == SagaStatus.COMPENSATING;
    }

    public Saga getSaga(String sagaId) {
        return activeSagas.get(sagaId);
    }

    public void removeSaga(String sagaId) {
        activeSagas.remove(sagaId);
    }
}