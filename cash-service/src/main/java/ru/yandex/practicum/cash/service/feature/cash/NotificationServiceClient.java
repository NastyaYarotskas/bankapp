package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationServiceClient {

    @Autowired
    @Qualifier("notificationServiceWebClient")
    private WebClient notificationServiceWebClient;

    public Mono<Void> sendNotification(NotificationRequest request) {
        return notificationServiceWebClient.post()
                .uri("/api/notifications")
                .body(Mono.just(request), NotificationRequest.class)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
