package ru.yandex.practicum.blocker.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.service.model.OperationCheckResult;
import ru.yandex.practicum.blocker.service.model.OperationContext;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockerOperationService {

    private final List<BlockerOperationDetector> detectors;

    public Mono<OperationCheckResult> checkOperation(OperationContext context) {
        return Flux.fromIterable(detectors)
                .flatMap(detector ->
                        detector.isOperationSuspicious(context)
                                .filter(Boolean.TRUE::equals)
                                .map(isSuspicious -> new OperationCheckResult(
                                        true,
                                        "Operation blocked by " + detector.getDetectionAlgorithmName(),
                                        detector.getDetectionAlgorithmName()
                                ))
                )
                .next()
                .switchIfEmpty(Mono.just(OperationCheckResult.ok()));
    }
}
