package ru.yandex.practicum.front.ui.feature.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TransferServiceClient {

    private final WebClient transferServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public TransferServiceClient(@LoadBalanced WebClient.Builder transferServiceWebClientBuilder) {
        this.transferServiceWebClient = transferServiceWebClientBuilder.build();
    }

    public Mono<Void> transfer(String login, TransferRequest request) {
        request.setLogin(login);
        return retrieveToken()
                .flatMap(
                        accessToken -> transferServiceWebClient.post()
                                .uri("/users/" + login + "/transfer")
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
