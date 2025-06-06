package ru.yandex.practicum.transfer.service.feature.transfer;

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
public class BlockerServiceClient {

    private final WebClient blockerServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public BlockerServiceClient(@LoadBalanced WebClient.Builder blockerServiceWebClientBuilder) {
        this.blockerServiceWebClient = blockerServiceWebClientBuilder.build();
    }

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
                        .withClientRegistrationId("transfer-service-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
