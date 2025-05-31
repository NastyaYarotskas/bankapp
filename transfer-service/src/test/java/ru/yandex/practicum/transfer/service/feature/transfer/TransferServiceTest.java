package ru.yandex.practicum.transfer.service.feature.transfer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.transfer.service.feature.transfer.model.Account;
import ru.yandex.practicum.transfer.service.feature.transfer.model.Currency;
import ru.yandex.practicum.transfer.service.feature.transfer.model.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TransferServiceTest {

    @MockitoBean
    private AccountsServiceClient accountsServiceClient;

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    @MockitoBean
    private BlockerServiceClient blockerServiceClient;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private TransferService transferService;

    @Test
    void transfer_validRequest_shouldTransferSuccessfully() {
        TransferRequest request = new TransferRequest();
        request.setLogin("user1");
        request.setToLogin("user2");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(1000);

        User fromUser = createUser("user1", "RUB", 2000);
        User toUser = createUser("user2", "USD", 100);

        Currency rub = new Currency("RUB", "RUB",1.0);
        Currency usd = new Currency("USD", "USD",75.0);

        when(accountsServiceClient.getAccountDetails("user1")).thenReturn(Mono.just(fromUser));
        when(accountsServiceClient.getAccountDetails("user2")).thenReturn(Mono.just(toUser));
        when(exchangeServiceClient.getCurrencyRates()).thenReturn(Flux.fromIterable(List.of(rub, usd)));
        when(blockerServiceClient.performOperation(any())).thenReturn(Mono.just(new OperationCheckResult(false, "OK", null)));
        when(accountsServiceClient.editUserAccounts(any(), any())).thenReturn(Mono.empty());
        when(notificationServiceClient.sendNotification(any())).thenReturn(Mono.empty());

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
        request.setLogin("user1");
        request.setToLogin("user2");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(2000);

        User fromUser = createUser("user1", "RUB", 1000);
        User toUser = createUser("user2", "USD", 100);

        when(accountsServiceClient.getAccountDetails("user1")).thenReturn(Mono.just(fromUser));
        when(accountsServiceClient.getAccountDetails("user2")).thenReturn(Mono.just(toUser));

        StepVerifier.create(transferService.transfer(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void transfer_blocked_shouldFail() {
        TransferRequest request = new TransferRequest();
        request.setLogin("user1");
        request.setToLogin("user2");
        request.setFromCurrency("RUB");
        request.setToCurrency("USD");
        request.setValue(1000);

        User fromUser = createUser("user1", "RUB", 2000);
        User toUser = createUser("user2", "USD", 100);

        when(accountsServiceClient.getAccountDetails("user1")).thenReturn(Mono.just(fromUser));
        when(accountsServiceClient.getAccountDetails("user2")).thenReturn(Mono.just(toUser));
        when(blockerServiceClient.performOperation(any()))
                .thenReturn(Mono.just(new OperationCheckResult(true, "Подозрительная операция", "algorithm1")));
        when(notificationServiceClient.sendNotification(any())).thenReturn(Mono.empty());

        StepVerifier.create(transferService.transfer(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    private User createUser(String login, String currency, double balance) {
        User user = new User();
        user.setLogin(login);
        Account account = new Account();
        account.setCurrency(new Currency(currency, currency, 1.0));
        account.setValue((int) balance);
        account.setExists(true);
        user.setAccounts(List.of(account));
        return user;
    }

}
