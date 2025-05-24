package ru.yandex.practicum.front.ui.feature.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.account.model.User;
import ru.yandex.practicum.front.ui.feature.account.request.CreateUserRequest;
import ru.yandex.practicum.front.ui.feature.account.request.EditPasswordRequest;

@Component
public class AccountsServiceClient {

    @Autowired
    private WebClient accountsServiceWebClient;

    public Mono<User> createUser(CreateUserRequest request) {
        return accountsServiceWebClient.post()
                .uri("/api/v1/users")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> getAccountDetails(String login) {
        return accountsServiceWebClient.get()
                .uri("/api/v1/users/" + login)
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<User> editPassword(String login, EditPasswordRequest request) {
        return accountsServiceWebClient.post()
                .uri("/api/v1/users/" + login + "/editPassword")
                .bodyValue(request)
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

    public Flux<User> getAllUsers() {
        return accountsServiceWebClient.get()
                .uri("/api/v1/users")
                .retrieve()
                .bodyToFlux(User.class);
    }
}
