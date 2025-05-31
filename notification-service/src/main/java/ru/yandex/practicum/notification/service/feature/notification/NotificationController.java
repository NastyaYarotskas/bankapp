package ru.yandex.practicum.notification.service.feature.notification;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @SneakyThrows
    @PostMapping
    public Mono<String> sendNotification(@RequestBody NotificationRequest request) {
        Thread.sleep(1000); // to emulate long-running operation
        messagingTemplate.convertAndSend("/topic/notifications", request);
        return Mono.just("Notification sent");
    }
}
