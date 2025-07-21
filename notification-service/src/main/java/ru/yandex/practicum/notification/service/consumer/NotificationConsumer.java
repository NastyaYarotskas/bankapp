package ru.yandex.practicum.notification.service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.model.NotificationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NotificationMetrics notificationMetrics;

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void listen(@Payload NotificationRequest request) {
        logger.info("Получено уведомление для пользователя: {}", request.getLogin());
        
        try {
            messagingTemplate.convertAndSend("/topic/notifications", request);
            logger.info("Уведомление успешно отправлено пользователю: {}", request.getLogin());
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления пользователю {}: {}", 
                    request.getLogin(), e.getMessage());
            notificationMetrics.incrementNotificationFailure(request.getLogin());
            throw e;
        }
    }
}
