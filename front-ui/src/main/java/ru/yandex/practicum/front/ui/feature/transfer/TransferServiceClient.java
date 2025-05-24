package ru.yandex.practicum.front.ui.feature.transfer;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TransferServiceClient {

    private final WebClient transferServiceWebClient;

    public TransferServiceClient(@LoadBalanced WebClient.Builder transferServiceWebClientBuilder) {
        this.transferServiceWebClient = transferServiceWebClientBuilder.build();
    }

    public Mono<Void> transfer(String login, TransferRequest request) {
        request.setLogin(login);
        return transferServiceWebClient.post()
                .uri("/users/" + login + "/transfer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
