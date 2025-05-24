package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AccountsServiceClient {

    private final WebClient accountsServiceWebClient;

    public AccountsServiceClient(@LoadBalanced WebClient.Builder accountsServiceWebClientBuilder) {
        this.accountsServiceWebClient = accountsServiceWebClientBuilder.build();
    }

    public Mono<User> getAccountDetails(String login) {
        return accountsServiceWebClient.get()
                .uri("/api/v1/users/" + login)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> editUserAccounts(String login, User request) {
        return accountsServiceWebClient.post()
                .uri("/api/v1/users/" + login + "/editUserAccounts")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(User.class);
    }
}
