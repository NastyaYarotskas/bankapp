package ru.yandex.practicum.transfer.service.feature.transfer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
public class TransferControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private TransferService transferService;

    @Test
    void transfer_validRequest_shouldReturnOk() {
        TransferRequest request = new TransferRequest();
        request.setLogin("user1");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(1000);
        request.setToLogin("user2");

        when(transferService.transfer(any(TransferRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/users/user1/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        verify(transferService).transfer(any(TransferRequest.class));
    }
}
