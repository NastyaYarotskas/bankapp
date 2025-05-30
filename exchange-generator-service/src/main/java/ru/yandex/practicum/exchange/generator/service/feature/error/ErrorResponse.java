package ru.yandex.practicum.exchange.generator.service.feature.error;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String path,
        Instant timestamp
) {}
