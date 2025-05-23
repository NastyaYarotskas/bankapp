package ru.yandex.practicum.front.ui.feature.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AccountClient {

    @Autowired
    private WebClient accountServiceWebClient;

    public Mono<User> createUser(CreateUserRequest request) {
        return accountServiceWebClient.post()
                .uri("/api/v1/users")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> getAccountDetails(String login) {
        return accountServiceWebClient.get()
                .uri("/api/v1/users/" + login)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> editPassword(String login, EditPasswordRequest request) {
        return accountServiceWebClient.post()
                .uri("/api/v1/users/" + login + "/editPassword")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> editUserAccounts(String login, User request) {
        return accountServiceWebClient.post()
                .uri("/api/v1/users/" + login + "/editUserAccounts")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(User.class);
    }
}
