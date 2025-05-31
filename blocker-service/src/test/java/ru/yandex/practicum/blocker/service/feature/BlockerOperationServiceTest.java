package ru.yandex.practicum.blocker.service.feature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest
public class BlockerOperationServiceTest {

    @Mock
    private BlockerOperationDetector detector1;

    @Mock
    private BlockerOperationDetector detector2;

    private BlockerOperationService service;
    private OperationContext context;

    @BeforeEach
    void setUp() {
        service = new BlockerOperationService(List.of(detector1, detector2));
        context = OperationContext.builder()
                .userId("user1")
                .operationType("PAYMENT")
                .amount(1000.0)
                .timestamp(OffsetDateTime.now())
                .metadata(Map.of("key", "value"))
                .build();
    }

    @Test
    void checkOperation_noDetectorsFlag_shouldReturnOk() {
        when(detector1.isOperationSuspicious(context)).thenReturn(Mono.just(false));
        when(detector2.isOperationSuspicious(context)).thenReturn(Mono.just(false));

        StepVerifier.create(service.checkOperation(context))
                .expectNext(OperationCheckResult.ok())
                .verifyComplete();
    }

    @Test
    void checkOperation_firstDetectorFlags_shouldReturnBlocked() {
        when(detector1.isOperationSuspicious(context)).thenReturn(Mono.just(true));
        when(detector1.getDetectionAlgorithmName()).thenReturn("DETECTOR_1");

        StepVerifier.create(service.checkOperation(context))
                .expectNext(new OperationCheckResult(
                        true,
                        "Operation blocked by DETECTOR_1",
                        "DETECTOR_1"))
                .verifyComplete();
    }

    @Test
    void checkOperation_secondDetectorFlags_shouldReturnBlocked() {
        when(detector1.isOperationSuspicious(context)).thenReturn(Mono.just(false));
        when(detector2.isOperationSuspicious(context)).thenReturn(Mono.just(true));
        when(detector2.getDetectionAlgorithmName()).thenReturn("DETECTOR_2");

        StepVerifier.create(service.checkOperation(context))
                .expectNext(new OperationCheckResult(
                        true,
                        "Operation blocked by DETECTOR_2",
                        "DETECTOR_2"))
                .verifyComplete();
    }

    @Test
    void checkOperation_noDetectors_shouldReturnOk() {
        service = new BlockerOperationService(Collections.emptyList());

        StepVerifier.create(service.checkOperation(context))
                .expectNext(OperationCheckResult.ok())
                .verifyComplete();
    }

    @Test
    void checkOperation_detectorReturnsError_shouldPropagateError() {
        when(detector1.isOperationSuspicious(context))
                .thenReturn(Mono.error(new RuntimeException("Test error")));

        StepVerifier.create(service.checkOperation(context))
                .expectError(RuntimeException.class)
                .verify();
    }
}
