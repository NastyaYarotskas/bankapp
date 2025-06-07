package ru.yandex.practicum.cash.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.service.request.OperationCheckResult;
import ru.yandex.practicum.cash.service.request.OperationRequest;

@Component
public class BlockerServiceClient extends BaseClient {

    @Autowired
    @Qualifier("blockerServiceWebClient")
    private WebClient blockerServiceWebClient;

    public Mono<OperationCheckResult> performOperation(OperationRequest operationRequest) {
        return retrieveToken()
                .flatMap(
                        accessToken -> blockerServiceWebClient.post()
                                .uri("/api/operations")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .body(Mono.just(operationRequest), OperationRequest.class)
                                .retrieve()
                                .bodyToMono(OperationCheckResult.class)
                );
    }
}
