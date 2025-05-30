package ru.yandex.practicum.blocker.service.feature;

import reactor.core.publisher.Mono;

public interface SuspiciousOperationDetector {
    Mono<Boolean> isOperationSuspicious(OperationContext context);
    String getDetectionAlgorithmName();
}
