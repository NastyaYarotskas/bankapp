package ru.yandex.practicum.front.ui.feature.account;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String path,
        Instant timestamp
) {}
