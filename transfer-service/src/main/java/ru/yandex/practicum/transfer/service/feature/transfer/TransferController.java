package ru.yandex.practicum.transfer.service.feature.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.transfer.service.feature.transfer.model.Account;
import ru.yandex.practicum.transfer.service.feature.transfer.model.Currency;
import ru.yandex.practicum.transfer.service.feature.transfer.model.User;

import java.util.List;

@RestController
public class TransferController {

    @Autowired
    private AccountsServiceClient accountsServiceClient;
    @Autowired
    private ExchangeServiceClient exchangeServiceClient;
    @Autowired
    private BlockerServiceClient blockerServiceClient;

    @PostMapping("/users/{login}/transfer")
    public Mono<Void> transfer(@RequestBody TransferRequest request) {
        return validateTransfer(request)
                .flatMap(this::processTransfer);
    }

    private Mono<Void> processTransfer(TransferRequest request) {
        return checkAccounts(request)
                .flatMap(usersTuple -> {
                    User fromUser = usersTuple.getT2();
                    OperationRequest operationRequest = new OperationRequest(
                        request.getLogin(),
                        "TRANSFER",
                        request.getValue()
                    );
                    
                    return blockerServiceClient.performOperation(operationRequest)
                        .flatMap(result -> {
                            if (result.blocked()) {
                                return Mono.error(new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Операция заблокирована: " + result.message()
                                ));
                            }
                            return getConvertedAmount(request)
                                    .flatMap(convertedAmount -> 
                                        executeTransfer(request, fromUser, convertedAmount));
                        });
                });
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
        // Уменьшаем баланс отправителя
        Account fromAccount = fromUser.getAccounts().stream()
                .filter(acc -> acc.getCurrency().getName().equals(request.getFromCurrency()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Счет отправителя не найден"
                ));
        
        fromAccount.setValue(fromAccount.getValue() - request.getValue());

        // Обновляем баланс отправителя и получателя
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

    private Mono<Tuple2<TransferRequest, User>> checkAccounts(TransferRequest request) {
        Mono<User> fromUser = accountsServiceClient.getAccountDetails(request.getLogin());

        Mono<User> toUser = accountsServiceClient.getAccountDetails(request.getToLogin());

        return Mono.zip(fromUser, toUser)
                .flatMap(tuple -> {
                    User from = tuple.getT1();
                    User to = tuple.getT2();

                    Account fromAccount = from.getAccounts().stream()
                            .filter(acc -> acc.getCurrency().getName().equals(request.getFromCurrency()))
                            .filter(Account::isExists)
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Аккаунт с валютой %s не найден".formatted(request.getFromCurrency())));

                    Account toAccount = to.getAccounts().stream()
                            .filter(acc -> acc.getCurrency().getName().equals(request.getToCurrency()))
                            .filter(Account::isExists)
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Аккаунт с валютой %s не найден".formatted(request.getToCurrency())));
                    
                    if (fromAccount.getValue() - request.getValue() < 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "На счету %s недостаточно средств".formatted(request.getFromCurrency())));
                    }

                    return Mono.just(Tuples.of(request, from, to));
                })
                .map(tuple -> Tuples.of(tuple.getT1(), tuple.getT2()));
    }
}