package ru.yandex.practicum.blocker.service.feature;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class OperationContext {
    private String userId;
    private String operationType;
    private double amount;
    private OffsetDateTime timestamp;
    private Map<String, String> metadata;
}
