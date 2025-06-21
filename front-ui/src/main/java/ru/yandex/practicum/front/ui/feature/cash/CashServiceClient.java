package ru.yandex.practicum.front.ui.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CashServiceClient {

    private final WebClient cashServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public CashServiceClient(WebClient.Builder cashServiceWebClientBuilder) {
        this.cashServiceWebClient = cashServiceWebClientBuilder.build();
    }

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> cashServiceWebClient.post()
                                .uri("/api/v1/users/" + login + "/cash")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(Void.class)
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
