package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationServiceClient {

    private final WebClient notificationServiceWebClient;

    public NotificationServiceClient(@LoadBalanced WebClient.Builder notificationServiceWebClientBuilder) {
        this.notificationServiceWebClient = notificationServiceWebClientBuilder.build();
    }

    public Mono<Void> sendNotification(NotificationRequest request) {
        return notificationServiceWebClient.post()
                .uri("/api/notifications")
                .body(Mono.just(request), NotificationRequest.class)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
