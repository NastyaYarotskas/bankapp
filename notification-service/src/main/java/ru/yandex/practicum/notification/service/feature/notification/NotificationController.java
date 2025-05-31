package ru.yandex.practicum.notification.service.feature.notification;

import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public Mono<String> sendNotification(@RequestBody NotificationRequest request) {
        messagingTemplate.convertAndSend("/topic/notifications", request);
        return Mono.just("Notification sent");
    }
}
