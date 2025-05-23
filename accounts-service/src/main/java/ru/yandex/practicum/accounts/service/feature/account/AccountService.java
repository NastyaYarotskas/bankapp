package ru.yandex.practicum.accounts.service.feature.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
                        return Mono.error(new RuntimeException("ID аккаунта не может быть null при обновлении"));
                    }
                    return accountRepository.findById(account.getId())
                            .switchIfEmpty(Mono.error(new RuntimeException("Аккаунт с ID " + account.getId() + " не найден")))
                            .flatMap(existingAccount -> {
                                account.setUserId(userId);
                                return accountRepository.save(account);
                            });
                });
    }
}