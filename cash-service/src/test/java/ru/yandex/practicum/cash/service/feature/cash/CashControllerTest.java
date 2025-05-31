package ru.yandex.practicum.cash.service.feature.cash;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
public class CashControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    CashService cashService;

    @Test
    void processAccountTransaction_putCash_shouldSuccessfullyProcessTransaction() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest(
                "USD",
                100,
                CashChangeRequest.Action.PUT
        );

        when(cashService.processAccountTransaction(eq(login), any(CashChangeRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/users/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void processAccountTransaction_emptyRequest_shouldReturnBadRequest() {
        String login = "testUser";

        webTestClient.post()
                .uri("/api/v1/users/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void processAccountTransaction_withdrawMoney_shouldSuccessfullyProcessTransaction() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest(
                "USD",
                50,
                CashChangeRequest.Action.GET
        );

        when(cashService.processAccountTransaction(eq(login), any(CashChangeRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/users/{login}/cash", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}
