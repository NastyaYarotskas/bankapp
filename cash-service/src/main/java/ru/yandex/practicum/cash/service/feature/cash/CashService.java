package ru.yandex.practicum.cash.service.feature.cash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CashService {
    private static final String ERROR_EMPTY_REQUEST = "Запрос или валюта не могут быть пустыми";
    private static final String ERROR_NEGATIVE_AMOUNT = "Сумма должна быть больше нуля";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Счет с указанной валютой не найден";
    private static final String ERROR_INSUFFICIENT_FUNDS = "На счету недостаточно средств";

    @Autowired
    private AccountsServiceClient accountsServiceClient;
    @Autowired
    private BlockerServiceClient blockerServiceClient;
    @Autowired
    private NotificationServiceClient notificationServiceClient;

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return validateRequest(request)
                .flatMap(validRequest ->
                        blockerServiceClient.performOperation(new OperationRequest(login, request.getAction().name(), request.getValue()))
                                .flatMap(checkResult -> {
                                    if (checkResult.blocked()) {
                                        sendErrorNotification(login, checkResult.message()); // Асинхронная отправка
                                        return Mono.error(new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                checkResult.message()
                                        ));
                                    }
                                    return processAccountOperation(login, validRequest);
                                }))
                .flatMap(result -> {
                    sendSuccessNotification(login, "Операция прошла успешно"); // Асинхронная отправка
                    return Mono.just(result);
                })
                .onErrorResume(error -> {
                    String errorMessage = error instanceof ResponseStatusException ?
                            ((ResponseStatusException) error).getReason() :
                            "Операция была отменена: " + error.getMessage();

                    sendErrorNotification(login, errorMessage); // Асинхронная отправка
                    return Mono.error(error);
                })
                .then();
    }

    // Асинхронная отправка успешного уведомления
    private void sendSuccessNotification(String login, String message) {
        notificationServiceClient.sendNotification(new NotificationRequest(login, message))
                .subscribe(
                        null,
                        e -> log.error("Failed to send success notification", e)
                );
    }

    // Асинхронная отправка уведомления об ошибке
    private void sendErrorNotification(String login, String message) {
        notificationServiceClient.sendNotification(new NotificationRequest(login, message))
                .subscribe(
                        null,
                        e -> log.error("Failed to send error notification", e)
                );
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