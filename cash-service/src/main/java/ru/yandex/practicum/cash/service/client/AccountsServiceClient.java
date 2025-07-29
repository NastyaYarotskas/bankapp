package ru.yandex.practicum.cash.service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.service.model.User;

@Component
public class AccountsServiceClient extends BaseClient {

    private final WebClient accountsServiceWebClient;

    public AccountsServiceClient(WebClient.Builder webClientBuilder, @Value("${account.service.url}") String baseUrl) {
        this.accountsServiceWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<User> getAccountDetails(String login) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsServiceWebClient.get()
                                .uri("/api/v1/users/" + login)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToMono(User.class)
                );
    }

    public Mono<User> editUserAccounts(String login, User request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsServiceWebClient.post()
                                .uri("/api/v1/users/" + login + "/editUserAccounts")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(User.class)
                );
    }
}
