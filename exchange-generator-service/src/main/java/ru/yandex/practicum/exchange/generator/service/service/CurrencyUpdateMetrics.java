package ru.yandex.practicum.exchange.generator.service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CurrencyUpdateMetrics {
    private final MeterRegistry meterRegistry;

    public CurrencyUpdateMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementCurrencyUpdateFailure(String currency) {
        Counter.builder("currency_update_failure_total")
                .description("Failed currency rate updates")
                .tag("currency", currency)
                .register(meterRegistry)
                .increment();
    }
} 