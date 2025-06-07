package ru.yandex.practicum.notification.service;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.notification.service.controller.NotificationController;
import ru.yandex.practicum.notification.service.request.NotificationRequest;

@SpringBootTest
@Import(TestSecurityConfig.class)
public class NotificationContractTest {

    @MockitoBean
    SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    NotificationController notificationController;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.standaloneSetup(notificationController);

        Mockito.doNothing().when(simpMessagingTemplate)
                .convertAndSend(Mockito.anyString(), Mockito.any(NotificationRequest.class));
    }
}
