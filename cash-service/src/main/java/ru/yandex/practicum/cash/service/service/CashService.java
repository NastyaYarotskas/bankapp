package ru.yandex.practicum.cash.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.service.client.AccountsServiceClient;
import ru.yandex.practicum.cash.service.client.BlockerServiceClient;
import ru.yandex.practicum.cash.service.model.Account;
import ru.yandex.practicum.cash.service.model.User;
import ru.yandex.practicum.cash.service.request.CashChangeRequest;
import ru.yandex.practicum.cash.service.request.OperationRequest;
import ru.yandex.practicum.model.NotificationRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {

    private static final Logger logger = LoggerFactory.getLogger(CashService.class);
    private static final String ERROR_EMPTY_REQUEST = "Запрос или валюта не могут быть пустыми";
    private static final String ERROR_NEGATIVE_AMOUNT = "Сумма должна быть больше нуля";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Счет с указанной валютой не найден";
    private static final String ERROR_INSUFFICIENT_FUNDS = "На счету недостаточно средств";

    @Autowired
    private AccountsServiceClient accountsServiceClient;
    @Autowired
    private BlockerServiceClient blockerServiceClient;
    @Autowired
    private NotificationProducer notificationProducer;

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        logger.info("Обработка транзакции: пользователь={}, валюта={}, действие={}, сумма={}",
                login, request.getCurrency(), request.getAction(), request.getValue());

        return validateRequest(request)
                .flatMap(validRequest -> blockerServiceClient
                        .performOperation(new OperationRequest(login, request.getAction().name(), request.getValue()))
                        .flatMap(checkResult -> {
                            if (checkResult.blocked()) {
                                logger.warn("Операция заблокирована: пользователь={}, валюта={}, причина={}",
                                        login, request.getCurrency(), checkResult.message());
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        checkResult.message()));
                            }
                            return processAccountOperation(login, validRequest);
                        }))
                .flatMap(result -> {
                    logger.info("Транзакция успешно выполнена: пользователь={}, валюта={}, действие={}",
                            login, request.getCurrency(), request.getAction());
                    sendNotification(login, "Операция прошла успешно");
                    return Mono.just(result);
                })
                .onErrorResume(error -> {
                    logger.error("Ошибка при выполнении транзакции: пользователь={}, валюта={}, ошибка={}",
                            login, request.getCurrency(), error.getMessage());
                    String errorMessage = error instanceof ResponseStatusException
                            ? ((ResponseStatusException) error).getReason()
                            : "Операция была отменена: " + error.getMessage();
                    sendNotification(login, errorMessage);
                    return Mono.error(error);
                })
                .then();
    }

    private void sendNotification(String login, String message) {
        notificationProducer.sendNotification(new NotificationRequest(login, message));
    }

    private Mono<CashChangeRequest> validateRequest(CashChangeRequest request) {
        return Mono.just(request)
                .filter(this::isValidRequest)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_EMPTY_REQUEST)))
                .filter(this::isPositiveAmount)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_NEGATIVE_AMOUNT)));
    }

    private boolean isValidRequest(CashChangeRequest request) {
        return request != null && request.getCurrency() != null;
    }

    private boolean isPositiveAmount(CashChangeRequest request) {
        return request.getValue() > 0;
    }

    private Mono<User> processAccountOperation(String login, CashChangeRequest request) {
        return accountsServiceClient.getAccountDetails(login)
                .flatMap(user -> {
                    Account account = findAccountByCurrency(user, request.getCurrency());
                    validateWithdrawal(account, request);
                    updateAccountBalance(account, request);
                    return accountsServiceClient.editUserAccounts(login, user);
                });
    }

    private Account findAccountByCurrency(User user, String currency) {
        return user.getAccounts().stream()
                .filter(acc -> acc.getCurrency() != null &&
                        acc.getCurrency().getName().equals(currency) &&
                        acc.isExists())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_ACCOUNT_NOT_FOUND));
    }

    private void validateWithdrawal(Account account, CashChangeRequest request) {
        if (isWithdrawal(request) && hasInsufficientFunds(account, request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERROR_INSUFFICIENT_FUNDS);
        }
    }

    private boolean isWithdrawal(CashChangeRequest request) {
        return request.getAction().equals(CashChangeRequest.Action.GET);
    }

    private boolean hasInsufficientFunds(Account account, CashChangeRequest request) {
        return account.getValue() < request.getValue();
    }

    private void updateAccountBalance(Account account, CashChangeRequest request) {
        int newBalance = calculateNewBalance(account, request);
        account.setValue(newBalance);
    }

    private int calculateNewBalance(Account account, CashChangeRequest request) {
        return request.getAction().equals(CashChangeRequest.Action.PUT)
                ? account.getValue() + request.getValue()
                : account.getValue() - request.getValue();
    }
}