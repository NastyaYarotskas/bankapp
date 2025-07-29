package ru.yandex.practicum.front.ui.feature.transfer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TransferMetrics {
    private final MeterRegistry meterRegistry;

    public TransferMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementFailedTransfer(String fromLogin, String toLogin, String fromAccount, String toAccount) {
        Counter.builder("transfer_failure_total")
                .description("Failed money transfers")
                .tag("from_login", fromLogin)
                .tag("to_login", toLogin)
                .tag("from_account", fromAccount)
                .tag("to_account", toAccount)
                .register(meterRegistry)
                .increment();
    }
} 