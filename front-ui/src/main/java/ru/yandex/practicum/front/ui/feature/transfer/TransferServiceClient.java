package ru.yandex.practicum.front.ui.feature.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TransferServiceClient {

    @Autowired
    private WebClient transferServiceWebClient;

    public Mono<Void> transfer(String login, TransferRequest request) {
        request.setLogin(login);
        return transferServiceWebClient.post()
                .uri("/users/" + login + "/transfer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
