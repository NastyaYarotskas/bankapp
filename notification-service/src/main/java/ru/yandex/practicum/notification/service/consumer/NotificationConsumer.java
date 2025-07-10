package ru.yandex.practicum.notification.service.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.NotificationRequest;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void listen(@Payload NotificationRequest request) {
        messagingTemplate.convertAndSend("/topic/notifications", request);
    }
}
