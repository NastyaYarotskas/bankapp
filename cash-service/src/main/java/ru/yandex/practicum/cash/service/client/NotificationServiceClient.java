package ru.yandex.practicum.cash.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.service.request.NotificationRequest;

@Component
public class NotificationServiceClient extends BaseClient {

    @Autowired
    @Qualifier("notificationServiceWebClient")
    private WebClient notificationServiceWebClient;

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
}
