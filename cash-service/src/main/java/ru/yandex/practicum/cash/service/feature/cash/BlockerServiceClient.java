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
public class BlockerServiceClient {

    @Autowired
    @Qualifier("blockerServiceWebClient")
    private WebClient blockerServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public Mono<OperationCheckResult> performOperation(OperationRequest operationRequest) {
        return retrieveToken()
                .flatMap(
                        accessToken -> blockerServiceWebClient.post()
                                .uri("/api/operations")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .body(Mono.just(operationRequest), OperationRequest.class)
                                .retrieve()
                                .bodyToMono(OperationCheckResult.class)
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
