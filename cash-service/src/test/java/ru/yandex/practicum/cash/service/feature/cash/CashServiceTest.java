package ru.yandex.practicum.cash.service.feature.cash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
                "ru.yandex.practicum:accounts-service:+:stubs:9003"
        },
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({TestConfig.class, TestSecurityConfig.class})
class CashServiceTest {

    @MockitoBean
    private ReactiveOAuth2AuthorizedClientManager manager;
    @Autowired
    private CashService cashService;

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
    void processAccountTransaction_requestIsValidAndOperationNotBlocked_shouldSuccessfullyProcessTransaction() {
        String login = "test_edit_user_login";
        CashChangeRequest request = new CashChangeRequest("USD", 100, CashChangeRequest.Action.PUT);

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .verifyComplete();
    }

    @Test
    void processAccountTransaction_operationBlocked_shouldReturnError() {
        String login = "test_edit_user_login";
        CashChangeRequest request = new CashChangeRequest("USD", 100000, CashChangeRequest.Action.PUT);
        String blockMessage = "Operation blocked";

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.FORBIDDEN &&
                        ((ResponseStatusException) throwable).getReason().contains(blockMessage)
                )
                .verify();
    }

    @Test
    void processAccountTransaction_invalidRequest_shouldReturnError() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest("USD", -100, CashChangeRequest.Action.PUT);

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST
                )
                .verify();
    }

    @Test
    void processAccountTransaction_insufficientFunds_shouldReturnError() {
        String login = "test_edit_user_login";
        CashChangeRequest request = new CashChangeRequest("USD", 1000, CashChangeRequest.Action.GET);

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                        ((ResponseStatusException) throwable).getReason().contains("недостаточно средств")
                )
                .verify();
    }

    @Test
    void processAccountTransaction_accountNotFound_shouldReturnError() {
        String login = "test_edit_user_login";
        CashChangeRequest request = new CashChangeRequest("EURO", 100, CashChangeRequest.Action.PUT);

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND
                )
                .verify();
    }
}
