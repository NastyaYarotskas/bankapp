package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class CashService {
    private static final String ERROR_EMPTY_REQUEST = "Запрос или валюта не могут быть пустыми";
    private static final String ERROR_NEGATIVE_AMOUNT = "Сумма должна быть больше нуля";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Счет с указанной валютой не найден";
    private static final String ERROR_INSUFFICIENT_FUNDS = "На счету недостаточно средств";

    @Autowired
    private AccountsServiceClient accountsServiceClient;

    public Mono<Void> processAccountTransaction(String login, CashChangeRequest request) {
        return validateRequest(request)
                .flatMap(validRequest -> processAccountOperation(login, validRequest))
                .then();
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