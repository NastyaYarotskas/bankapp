package ru.yandex.practicum.notification.service.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.model.NotificationRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NotificationMetrics notificationMetrics;

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void listen(@Payload NotificationRequest request) {
        log.info("Got notification: {}", request);
        try {
            messagingTemplate.convertAndSend("/topic/notifications", request);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", request.getLogin(), e.getMessage());
            notificationMetrics.incrementNotificationFailure(request.getLogin());
        }
    }
}
