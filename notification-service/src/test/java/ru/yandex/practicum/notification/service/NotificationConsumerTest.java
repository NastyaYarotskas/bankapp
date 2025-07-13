package ru.yandex.practicum.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.model.NotificationRequest;

import java.util.concurrent.ExecutionException;

@SpringBootTest
@EmbeddedKafka(
        topics = {"notifications"}
)
public class NotificationConsumerTest {

    @MockitoBean
    SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Test
    public void sendNotification_shouldReadFromTopic() throws ExecutionException, InterruptedException {
        NotificationRequest request = new NotificationRequest("test_user", "test_msg");
        kafkaTemplate.send("notifications", request).get();
    }
}
