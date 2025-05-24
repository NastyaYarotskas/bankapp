package ru.yandex.practicum.transfer.service.feature.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.transfer.service.feature.transfer.model.User;

@Component
public class AccountsServiceClient {

    @Autowired
    private WebClient accountsServiceWebClient;

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
