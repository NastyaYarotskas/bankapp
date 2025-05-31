package ru.yandex.practicum.cash.service.feature.cash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class CashServiceTest {

    @Mock
    private AccountsServiceClient accountsServiceClient;

    @Mock
    private BlockerServiceClient blockerServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private CashService cashService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(notificationServiceClient.sendNotification(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void processAccountTransaction_requestIsValidAndOperationNotBlocked_shouldSuccessfullyProcessTransaction() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest("Доллары", 100, CashChangeRequest.Action.PUT);
        User user = createTestUser();

        when(blockerServiceClient.performOperation(any()))
                .thenReturn(Mono.just(new OperationCheckResult(false, null, null)));
        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(user));
        when(accountsServiceClient.editUserAccounts(anyString(), any()))
                .thenReturn(Mono.just(user));

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .verifyComplete();

        verify(notificationServiceClient).sendNotification(
                argThat(notification -> notification.message().contains("Операция прошла успешно"))
        );
    }

    @Test
    void processAccountTransaction_operationBlocked_shouldReturnError() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest("Доллары", 100, CashChangeRequest.Action.PUT);
        String blockMessage = "Operation blocked";

        when(blockerServiceClient.performOperation(any()))
                .thenReturn(Mono.just(new OperationCheckResult(true, blockMessage, "test")));

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.FORBIDDEN &&
                        ((ResponseStatusException) throwable).getReason().equals(blockMessage)
                )
                .verify();

        verify(notificationServiceClient).sendNotification(
                argThat(notification -> notification.message().equals(blockMessage))
        );
    }

    @Test
    void processAccountTransaction_invalidRequest_shouldReturnError() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest("Доллары", -100, CashChangeRequest.Action.PUT);

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST
                )
                .verify();
    }

    @Test
    void processAccountTransaction_insufficientFunds_shouldReturnError() {
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest("Доллары", 1000, CashChangeRequest.Action.GET);
        User user = createTestUser();

        when(blockerServiceClient.performOperation(any()))
                .thenReturn(Mono.just(new OperationCheckResult(false, null, null)));
        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(user));

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
        String login = "testUser";
        CashChangeRequest request = new CashChangeRequest("Евро", 100, CashChangeRequest.Action.PUT);
        User user = createTestUser();

        when(blockerServiceClient.performOperation(any()))
                .thenReturn(Mono.just(new OperationCheckResult(false, null, null)));
        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(user));

        StepVerifier.create(cashService.processAccountTransaction(login, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND
                )
                .verify();
    }

    private User createTestUser() {
        User user = new User();
        Account account = new Account();
        account.setCurrency(new Currency("USD", "Доллары"));
        account.setValue(500);
        account.setExists(true);
        user.setAccounts(List.of(account));
        return user;
    }
}
