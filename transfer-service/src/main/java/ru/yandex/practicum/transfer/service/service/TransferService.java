package ru.yandex.practicum.transfer.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.model.NotificationRequest;
import ru.yandex.practicum.transfer.service.client.*;
import ru.yandex.practicum.transfer.service.request.OperationRequest;
import ru.yandex.practicum.transfer.service.request.TransferRequest;
import ru.yandex.practicum.transfer.service.model.Account;
import ru.yandex.practicum.transfer.service.model.Currency;
import ru.yandex.practicum.transfer.service.model.User;
import ru.yandex.practicum.transfer.service.response.OperationCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private static final String TRANSFER_OPERATION = "TRANSFER";
    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Аккаунт с валютой %s не найден";
    private static final String INSUFFICIENT_FUNDS_MESSAGE = "На счету %s недостаточно средств";
    
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final AccountsServiceClient accountsServiceClient;
    private final ExchangeServiceClient exchangeServiceClient;
    private final BlockerServiceClient blockerServiceClient;
    private final NotificationProducer notificationProducer;

    public Mono<Void> transfer(TransferRequest request) {
        logger.info("Выполнение перевода: пользователь={}, от={}, к={}, сумма={}, валюта={}", 
                request.getLogin(), request.getFromCurrency(), request.getToCurrency(), request.getValue(), request.getToLogin());
        
        return validateTransfer(request)
                .flatMap(this::processTransfer)
                .doOnSuccess(result -> logger.info("Перевод успешно выполнен: пользователь={}, сумма={}", 
                        request.getLogin(), request.getValue()))
                .doOnError(e -> logger.error("Ошибка при выполнении перевода: пользователь={}, ошибка={}", 
                        request.getLogin(), e.getMessage()));
    }

    private Mono<Void> processTransfer(TransferRequest request) {
        return checkAccounts(request)
                .flatMap(usersTuple -> processBlockerCheck(request, usersTuple.getT2()))
                .onErrorResume(error -> handleTransferError(error, request));
    }

    private Mono<Void> processBlockerCheck(TransferRequest request, User fromUser) {
        return blockerServiceClient.performOperation(createOperationRequest(request))
                .flatMap(result -> handleBlockerResult(result, request, fromUser));
    }

    private OperationRequest createOperationRequest(TransferRequest request) {
        return new OperationRequest(request.getLogin(), TRANSFER_OPERATION, request.getValue());
    }

    private Mono<Void> handleBlockerResult(OperationCheckResult result, TransferRequest request, User fromUser) {
        if (result.blocked()) {
            sendTransferNotification(request.getLogin(), "Перевод заблокирован: " + result.message());
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Операция заблокирована: " + result.message()));
        }
        return getConvertedAmount(request)
                .flatMap(amount -> executeTransfer(request, fromUser, amount)
                        .doOnSuccess(__ -> sendSuccessNotification(request)));
    }

    private void sendTransferNotification(String login, String message) {
        notificationProducer.sendNotification(new NotificationRequest(login, message));
    }

    private void sendSuccessNotification(TransferRequest request) {
        sendTransferNotification(request.getLogin(), 
            "Перевод успешно выполнен на сумму " + request.getValue());
    }

    private Account findAccountByCurrency(User user, String currencyName, String errorMessage) {
        return user.getAccounts().stream()
                .filter(acc -> acc.getCurrency().getName().equals(currencyName))
                .filter(Account::isExists)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    String.format(errorMessage, currencyName)));
    }

    private Mono<Tuple2<TransferRequest, User>> checkAccounts(TransferRequest request) {
        return Mono.zip(
                accountsServiceClient.getAccountDetails(request.getLogin()),
                accountsServiceClient.getAccountDetails(request.getToLogin())
        ).flatMap(tuple -> validateAccounts(request, tuple.getT1(), tuple.getT2()));
    }

    private Mono<Tuple2<TransferRequest, User>> validateAccounts(TransferRequest request, 
            User fromUser, User toUser) {
        Account fromAccount = findAccountByCurrency(fromUser, request.getFromCurrency(), 
            ACCOUNT_NOT_FOUND_MESSAGE);
        findAccountByCurrency(toUser, request.getToCurrency(), ACCOUNT_NOT_FOUND_MESSAGE);

        if (fromAccount.getValue() - request.getValue() < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(INSUFFICIENT_FUNDS_MESSAGE, request.getFromCurrency())));
        }
        return Mono.just(Tuples.of(request, fromUser));
    }

    private Mono<Void> handleTransferError(Throwable error, TransferRequest request) {
        String errorMessage = error instanceof ResponseStatusException
                ? error.getMessage()
                : "Ошибка при переводе: " + error.getMessage();
        sendTransferNotification(request.getLogin(), errorMessage);
        return Mono.error(error);
    }

    private Mono<Double> getConvertedAmount(TransferRequest request) {
        return exchangeServiceClient.getCurrencyRates()
                .collectList()
                .flatMap(rates -> {
                    Currency fromCurrency = findCurrency(rates, request.getFromCurrency());
                    Currency toCurrency = findCurrency(rates, request.getToCurrency());
                    double convertedAmount = (request.getValue() * fromCurrency.getValue()) / toCurrency.getValue();
                    return Mono.just(convertedAmount);
                });
    }

    private Currency findCurrency(List<Currency> rates, String currencyName) {
        return rates.stream()
                .filter(c -> c.getName().equals(currencyName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Валюта " + currencyName + " не найдена"
                ));
    }

    private Mono<Void> executeTransfer(TransferRequest request, User fromUser, double convertedAmount) {
        Account fromAccount = fromUser.getAccounts().stream()
                .filter(acc -> acc.getCurrency().getName().equals(request.getFromCurrency()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Счет отправителя не найден"
                ));

        fromAccount.setValue(fromAccount.getValue() - request.getValue());

        return accountsServiceClient.editUserAccounts(request.getLogin(), fromUser)
                .then(updateRecipientBalance(request, convertedAmount));
    }

    private Mono<Void> updateRecipientBalance(TransferRequest request, double convertedAmount) {
        return accountsServiceClient.getAccountDetails(request.getToLogin())
                .flatMap(toUser -> {
                    Account toAccount = toUser.getAccounts().stream()
                            .filter(acc -> acc.getCurrency().getName().equals(request.getToCurrency()))
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Счет получателя не найден"
                            ));

                    toAccount.setValue(toAccount.getValue() + (int) convertedAmount);
                    return accountsServiceClient.editUserAccounts(request.getToLogin(), toUser);
                })
                .then();
    }

    private Mono<TransferRequest> validateTransfer(TransferRequest request) {
        if (request.getValue() < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Значение перевода должно быть положительным"));
        }
        return Mono.just(request);
    }
}