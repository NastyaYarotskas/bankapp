package ru.yandex.practicum.blocker.service.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.service.model.OperationContext;

public interface BlockerOperationDetector {

    Mono<Boolean> isOperationSuspicious(OperationContext context);

    String getDetectionAlgorithmName();
}
