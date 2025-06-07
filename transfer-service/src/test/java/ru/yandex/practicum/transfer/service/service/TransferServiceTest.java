package ru.yandex.practicum.transfer.service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.transfer.service.config.TestWebClientConfig;
import ru.yandex.practicum.transfer.service.config.TestOAuth2ClientConfig;
import ru.yandex.practicum.transfer.service.request.TransferRequest;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@SpringBootTest
@AutoConfigureStubRunner(
        ids = {
                "ru.yandex.practicum:blocker-service:+:stubs:9002",
                "ru.yandex.practicum:notification-service:+:stubs:9001",
                "ru.yandex.practicum:accounts-service:+:stubs:9003",
                "ru.yandex.practicum:exchange-service:+:stubs:9004"
        },
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({TestWebClientConfig.class, TestOAuth2ClientConfig.class})
public class TransferServiceTest {

    @MockitoBean
    private ReactiveOAuth2AuthorizedClientManager manager;

    @Autowired
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        OAuth2AccessToken token = new OAuth2AccessToken(
                BEARER, "mock-token", Instant.now(), Instant.now().plusSeconds(300));

        when(manager.authorize(any()))
                .thenReturn(Mono.just(new OAuth2AuthorizedClient(
                        mock(ClientRegistration.class),
                        "principal",
                        token)));
    }

    @Test
    void transfer_validRequest_shouldTransferSuccessfully() {
        TransferRequest request = new TransferRequest();
        request.setLogin("test_edit_user_login");
        request.setToLogin("test_edit_user_login");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(10);

        StepVerifier.create(transferService.transfer(request))
                .verifyComplete();
    }

    @Test
    void transfer_negativeAmount_shouldFail() {
        TransferRequest request = new TransferRequest();
        request.setValue(-100);

        StepVerifier.create(transferService.transfer(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void transfer_insufficientFunds_shouldFail() {
        TransferRequest request = new TransferRequest();
        request.setLogin("test_edit_user_login");
        request.setToLogin("test_edit_user_login");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(2000);

        StepVerifier.create(transferService.transfer(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void transfer_blocked_shouldFail() {
        TransferRequest request = new TransferRequest();
        request.setLogin("test_edit_user_login");
        request.setToLogin("test_edit_user_login");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(1000000);

        StepVerifier.create(transferService.transfer(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }
}
