package com.ecommerce.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public abstract class DomainEvent {

    private String eventId;
    private String eventType;
    private String aggregateId;
    private String aggregateType;
    private Long version;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurredOn;

    protected DomainEvent(String aggregateId, String aggregateType, Long version) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.occurredOn = LocalDateTime.now();
    }
}