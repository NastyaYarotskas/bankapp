package ru.yandex.practicum.blocker.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.service.model.OperationCheckResult;
import ru.yandex.practicum.blocker.service.model.OperationContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockerOperationService {

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BlockerOperationService.class);

        private final List<BlockerOperationDetector> detectors;

        @Autowired
        private BlockerMetrics blockerMetrics;

        public Mono<OperationCheckResult> checkOperation(OperationContext context) {
                logger.info("Проверка операции на блокировку: пользователь={}, операция={}", context.getUserId(),
                                context.getOperationType());

                return Flux.fromIterable(detectors)
                                .flatMap(detector -> detector.isOperationSuspicious(context)
                                                .filter(Boolean.TRUE::equals)
                                                .map(isSuspicious -> {
                                                        // Метрика блокировки
                                                        String fromAccount = context.getMetadata() != null
                                                                        ? context.getMetadata().getOrDefault(
                                                                                        "fromAccount", "unknown")
                                                                        : "unknown";
                                                        String toAccount = context.getMetadata() != null
                                                                        ? context.getMetadata().getOrDefault(
                                                                                        "toAccount", "unknown")
                                                                        : "unknown";
                                                        blockerMetrics.incrementBlockedOperation(
                                                                        context.getUserId(),
                                                                        context.getOperationType(),
                                                                        fromAccount,
                                                                        toAccount);
                                                        logger.warn("Операция заблокирована: пользователь={}, операция={}, причина={}",
                                                                        context.getUserId(), context.getOperationType(),
                                                                        "Operation blocked by " + detector
                                                                                        .getDetectionAlgorithmName());
                                                        return new OperationCheckResult(
                                                                        true,
                                                                        "Operation blocked by " + detector
                                                                                        .getDetectionAlgorithmName(),
                                                                        detector.getDetectionAlgorithmName());
                                                }))
                                .next()
                                .switchIfEmpty(Mono.just(OperationCheckResult.ok()))
                                .doOnSuccess(response -> {
                                        if (response.blocked()) {
                                                logger.warn("Операция заблокирована: пользователь={}, операция={}, причина={}",
                                                                context.getUserId(), context.getOperationType(),
                                                                response.message());
                                        } else {
                                                logger.info("Операция разрешена: пользователь={}, операция={}",
                                                                context.getUserId(), context.getOperationType());
                                        }
                                })
                                .doOnError(e -> logger.error(
                                                "Ошибка при проверке операции: пользователь={}, операция={}, ошибка={}",
                                                context.getUserId(), context.getOperationType(), e.getMessage()));
        }
}
