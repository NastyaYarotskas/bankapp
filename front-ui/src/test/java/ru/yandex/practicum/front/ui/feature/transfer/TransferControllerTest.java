package ru.yandex.practicum.front.ui.feature.transfer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.TestSecurityConfig;
import ru.yandex.practicum.front.ui.feature.auth.CustomUserDetails;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class TransferControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TransferServiceClient transferServiceClient;

    @Test
    void transfer_successfulTransfer_shouldRedirectToMain() {
        String login = "testUser";
        CustomUserDetails userDetails = new CustomUserDetails(login, "password",
                true, true, true, true, List.of());

        TransferRequest request = new TransferRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("RUB");
        request.setValue(100);
        request.setToLogin("recipient");

        when(transferServiceClient.transfer(eq(login), any(TransferRequest.class)))
                .thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/transfer", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("fromCurrency=" + request.getFromCurrency() +
                           "&toCurrency=" + request.getToCurrency() +
                           "&value=" + request.getValue() +
                           "&toLogin=" + request.getToLogin())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");

        verify(transferServiceClient).transfer(eq(login), any(TransferRequest.class));
    }

    @Test
    void transfer_whenNotAuthenticated_shouldRedirectToLogin() {
        String login = "testUser";
        TransferRequest request = new TransferRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("RUB");
        request.setValue(100);
        request.setToLogin("recipient");

        webTestClient
                .post()
                .uri("/user/{login}/transfer", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("fromCurrency=" + request.getFromCurrency() +
                           "&toCurrency=" + request.getToCurrency() +
                           "&value=" + request.getValue() +
                           "&toLogin=" + request.getToLogin())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        verify(transferServiceClient, never()).transfer(anyString(), any());
    }
}
