package ru.yandex.practicum.front.ui.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CashServiceClient {

    @Autowired
    private WebClient cashServiceWebClient;

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return cashServiceWebClient.post()
                .uri("/api/v1/users/" + login + "/cash")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
