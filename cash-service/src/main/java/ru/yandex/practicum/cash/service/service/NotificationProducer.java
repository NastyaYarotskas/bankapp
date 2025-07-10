package ru.yandex.practicum.cash.service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.NotificationRequest;

@Service
public class NotificationProducer {
    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationRequest request) {
        kafkaTemplate.send("notifications", request);
    }
}
