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
public class NotificationServiceClient {

    private final WebClient notificationServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public NotificationServiceClient(@LoadBalanced WebClient.Builder notificationServiceWebClientBuilder) {
        this.notificationServiceWebClient = notificationServiceWebClientBuilder.build();
    }

    public Mono<Void> sendNotification(NotificationRequest request) {
        return retrieveToken()
                .flatMap(
                        accessToken -> notificationServiceWebClient.post()
                                .uri("/api/notifications")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .body(Mono.just(request), NotificationRequest.class)
                                .retrieve()
                                .bodyToMono(Void.class)
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
