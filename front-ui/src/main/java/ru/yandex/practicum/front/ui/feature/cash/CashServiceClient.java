package ru.yandex.practicum.front.ui.feature.cash;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CashServiceClient {

    private final WebClient cashServiceWebClient;

    public CashServiceClient(@LoadBalanced WebClient.Builder cashServiceWebClientBuilder) {
        this.cashServiceWebClient = cashServiceWebClientBuilder.build();
    }

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return cashServiceWebClient.post()
                .uri("/api/v1/users/" + login + "/cash")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
