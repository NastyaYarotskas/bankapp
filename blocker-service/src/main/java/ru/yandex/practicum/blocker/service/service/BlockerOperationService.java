package ru.yandex.practicum.blocker.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.service.model.OperationCheckResult;
import ru.yandex.practicum.blocker.service.model.OperationContext;

@Service
@RequiredArgsConstructor
public class BlockerOperationService {

    private final List<BlockerOperationDetector> detectors;

    @Autowired
    private BlockerMetrics blockerMetrics;

    public Mono<OperationCheckResult> checkOperation(OperationContext context) {
        return Flux.fromIterable(detectors)
                .flatMap(detector ->
                        detector.isOperationSuspicious(context)
                                .filter(Boolean.TRUE::equals)
                                .map(isSuspicious -> {
                                    // Метрика блокировки
                                    String fromAccount = context.getMetadata() != null ? context.getMetadata().getOrDefault("fromAccount", "unknown") : "unknown";
                                    String toAccount = context.getMetadata() != null ? context.getMetadata().getOrDefault("toAccount", "unknown") : "unknown";
                                    blockerMetrics.incrementBlockedOperation(
                                        context.getUserId(),
                                        context.getOperationType(),
                                        fromAccount,
                                        toAccount
                                    );
                                    return new OperationCheckResult(
                                            true,
                                            "Operation blocked by " + detector.getDetectionAlgorithmName(),
                                            detector.getDetectionAlgorithmName()
                                    );
                                })
                )
                .next()
                .switchIfEmpty(Mono.just(OperationCheckResult.ok()));
    }
}
