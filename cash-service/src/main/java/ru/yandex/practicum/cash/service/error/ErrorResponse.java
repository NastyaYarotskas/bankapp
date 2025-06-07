package ru.yandex.practicum.cash.service.error;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String path,
        Instant timestamp
) {}
