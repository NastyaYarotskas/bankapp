package ru.yandex.practicum.blocker.service.feature;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
public class RandomBlockerOperationDetector implements BlockerOperationDetector {

    private final Random random = new Random();

    @Override
    public Mono<Boolean> isOperationSuspicious(OperationContext context) {
        return Mono.just(random.nextDouble() < 0.3); // 30% вероятность
    }

    @Override
    public String getDetectionAlgorithmName() {
        return "RANDOM_DETECTOR";
    }
}
