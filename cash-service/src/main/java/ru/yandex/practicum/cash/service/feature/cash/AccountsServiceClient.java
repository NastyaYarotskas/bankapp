package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AccountsServiceClient {

    @Autowired
    @Qualifier("accountsServiceWebClient")
    private WebClient accountsServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

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

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("cash-service-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
