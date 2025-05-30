package ru.yandex.practicum.transfer.service.feature.transfer;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BlockerServiceClient {

    private final WebClient blockerServiceWebClient;

    public BlockerServiceClient(@LoadBalanced WebClient.Builder blockerServiceWebClientBuilder) {
        this.blockerServiceWebClient = blockerServiceWebClientBuilder.build();
    }

    public Mono<OperationCheckResult> performOperation(OperationRequest operationRequest) {
        return blockerServiceWebClient.post()
                .uri("/api/operations")
                .body(Mono.just(operationRequest), OperationRequest.class)
                .retrieve()
                .bodyToMono(OperationCheckResult.class);
    }
}
