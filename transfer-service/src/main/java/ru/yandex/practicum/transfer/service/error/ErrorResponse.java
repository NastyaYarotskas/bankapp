package ru.yandex.practicum.transfer.service.error;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String path,
        Instant timestamp
) {}
