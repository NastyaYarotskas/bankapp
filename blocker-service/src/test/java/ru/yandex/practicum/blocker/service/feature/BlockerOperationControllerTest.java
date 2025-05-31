package ru.yandex.practicum.blocker.service.feature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
public class BlockerOperationControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private BlockerOperationService blockerOperationService;

    @Test
    void performOperation_operationAllowed_shouldReturnOkResult() {
        OperationRequest request = new OperationRequest("user123", "PAYMENT", 100.0);
        OperationCheckResult expectedResult = OperationCheckResult.ok();

        when(blockerOperationService.checkOperation(any()))
                .thenReturn(Mono.just(expectedResult));

        webTestClient.post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OperationCheckResult.class)
                .isEqualTo(expectedResult);
    }

    @Test
    void performOperation_operationBlocked_shouldReturnBlockedResult() {
        OperationRequest request = new OperationRequest("user123", "PAYMENT", 1000000.0);
        OperationCheckResult blockedResult = new OperationCheckResult(
                true,
                "Operation blocked by Large Amount Detector",
                "LARGE_AMOUNT_DETECTOR"
        );

        when(blockerOperationService.checkOperation(any()))
                .thenReturn(Mono.just(blockedResult));

        webTestClient.post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OperationCheckResult.class)
                .isEqualTo(blockedResult);
    }

    @Test
    void performOperation_invalidRequest_shouldReturnBadRequest() {
        String invalidJson = "{\"userId\": \"user123\", invalid}";

        webTestClient.post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void performOperation_emptyRequest_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
