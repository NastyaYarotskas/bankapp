package ru.yandex.practicum.accounts.service.feature.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.ACCOUNT_WITH_ID_NOT_SOUND_FORMAT_ERROR_MSG;
import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.INVALID_ACCOUNT_LIST_ERROR_MSG;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Flux<AccountEntity> createAccounts(Flux<AccountEntity> accounts) {
        return accountRepository.saveAll(accounts);
    }

    public Flux<AccountEntity> findByUserId(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    public Flux<AccountEntity> updateAccounts(UUID userId, Flux<AccountEntity> accounts) {
        return accounts
                .flatMap(account -> {
                    if (account.getId() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ACCOUNT_LIST_ERROR_MSG));
                    }
                    return accountRepository.findFirstByUserIdAndCurrency(userId, account.getCurrency())
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    ACCOUNT_WITH_ID_NOT_SOUND_FORMAT_ERROR_MSG.formatted(account.getId()))))
                            .flatMap(existingAccount -> {
                                account.setUserId(userId);
                                return accountRepository.save(account);
                            });
                });
    }
}