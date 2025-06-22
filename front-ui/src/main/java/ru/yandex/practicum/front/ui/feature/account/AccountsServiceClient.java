package ru.yandex.practicum.front.ui.feature.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.account.model.User;
import ru.yandex.practicum.front.ui.feature.account.request.CreateUserRequest;
import ru.yandex.practicum.front.ui.feature.account.request.EditPasswordRequest;

@Component
public class AccountsServiceClient {

    private final WebClient accountsServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public AccountsServiceClient(WebClient.Builder accountsServiceWebClientBuilder) {
        this.accountsServiceWebClient = accountsServiceWebClientBuilder.build();
    }

    public Mono<User> createUser(CreateUserRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsServiceWebClient.post()
                                .uri("/api/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(User.class)
                );
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

    public Mono<User> editPassword(String login, EditPasswordRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> accountsServiceWebClient.post()
                                .uri("/api/v1/users/" + login + "/editPassword")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
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

    public Flux<User> getAllUsers() {
        return retrieveToken()
                .flatMapMany(
                        accessToken -> accountsServiceWebClient.get()
                                .uri("/api/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToFlux(User.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("front-ui-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
