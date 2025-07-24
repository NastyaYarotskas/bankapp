package ru.yandex.practicum.accounts.service.service;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.service.entity.AccountEntity;
import ru.yandex.practicum.accounts.service.repository.AccountRepository;

import java.util.UUID;

import static ru.yandex.practicum.accounts.service.message.UserValidationErrorMessages.ACCOUNT_WITH_ID_NOT_SOUND_FORMAT_ERROR_MSG;
import static ru.yandex.practicum.accounts.service.message.UserValidationErrorMessages.INVALID_ACCOUNT_LIST_ERROR_MSG;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final Tracer tracer;

    public AccountService(AccountRepository accountRepository, Tracer tracer) {
        this.accountRepository = accountRepository;
        this.tracer = tracer;
    }

    public Flux<AccountEntity> createAccounts(Flux<AccountEntity> accounts) {
        Span parent = tracer.currentSpan();
        Span span = tracer.nextSpan(parent).name("create-accounts").start();

        return accountRepository.saveAll(accounts)
                .doOnSubscribe(sub -> span.tag("event", "Started DB call"))
                .doOnComplete(() -> span.tag("event", "Completed DB call"))
                .doOnError(error -> {
                    span.tag("error", error.getMessage());
                    span.error(error);
                })
                .doFinally(signalType -> {
                    span.tag("event", "Finished signal: " + signalType.name());
                    span.end();
                });
    }

    public Flux<AccountEntity> findByUserId(UUID userId) {
        Span parent = tracer.currentSpan();
        Span span = tracer.nextSpan(parent).name("find-accounts-by-user-id").start();
        
        return accountRepository.findByUserId(userId)
                .doOnSubscribe(sub -> span.tag("event", "Started DB call"))
                .doOnComplete(() -> span.tag("event", "Completed DB call"))
                .doOnError(error -> {
                    span.tag("error", error.getMessage());
                    span.error(error);
                })
                .doFinally(signalType -> {
                    span.tag("event", "Finished signal: " + signalType.name());
                    span.end();
                });
    }

    public Flux<AccountEntity> updateAccounts(UUID userId, Flux<AccountEntity> accounts) {
        Span parent = tracer.currentSpan();
        Span span = tracer.nextSpan(parent).name("update-accounts").start();

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
                })
                .doOnSubscribe(sub -> span.tag("event", "Started DB call"))
                .doOnComplete(() -> span.tag("event", "Completed DB call"))
                .doOnError(error -> {
                    span.tag("error", error.getMessage());
                    span.error(error);
                })
                .doFinally(signalType -> {
                    span.tag("event", "Finished signal: " + signalType.name());
                    span.end();
                });
    }
}