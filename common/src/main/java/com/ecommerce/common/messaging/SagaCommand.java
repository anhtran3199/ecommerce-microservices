package com.ecommerce.common.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaCommand {

    private String commandId;
    private String commandType;
    private String sagaId;
    private String targetService;
    private Object payload;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public SagaCommand(String commandType, String sagaId, String targetService, Object payload) {
        this.commandId = UUID.randomUUID().toString();
        this.commandType = commandType;
        this.sagaId = sagaId;
        this.targetService = targetService;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }
}