package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BlockerServiceClient {

    @Autowired
    @Qualifier("blockerServiceWebClient")
    private WebClient blockerServiceWebClient;

    public Mono<OperationCheckResult> performOperation(OperationRequest operationRequest) {
        return blockerServiceWebClient.post()
                .uri("/api/operations")
                .body(Mono.just(operationRequest), OperationRequest.class)
                .retrieve()
                .bodyToMono(OperationCheckResult.class);
    }
}
