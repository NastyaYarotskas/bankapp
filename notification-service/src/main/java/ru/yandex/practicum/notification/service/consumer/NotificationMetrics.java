package ru.yandex.practicum.notification.service.consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {
    private final MeterRegistry meterRegistry;

    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementNotificationFailure(String login) {
        Counter.builder("notification_failure_total")
                .description("Failed notification sends")
                .tag("login", login)
                .register(meterRegistry)
                .increment();
    }
} 