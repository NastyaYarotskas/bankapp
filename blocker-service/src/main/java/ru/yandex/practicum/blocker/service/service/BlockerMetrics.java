package ru.yandex.practicum.blocker.service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BlockerMetrics {
    private final MeterRegistry meterRegistry;

    public BlockerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementBlockedOperation(String userId, String operationType, String fromAccount, String toAccount) {
        Counter.builder("blocked_operation_total")
                .description("Blocked suspicious operations")
                .tag("userId", userId)
                .tag("operationType", operationType)
                .tag("fromAccount", fromAccount == null ? "unknown" : fromAccount)
                .tag("toAccount", toAccount == null ? "unknown" : toAccount)
                .register(meterRegistry)
                .increment();
    }
} 