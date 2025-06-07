package ru.yandex.practicum.exchange.service.error;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String path,
        Instant timestamp
) {}
