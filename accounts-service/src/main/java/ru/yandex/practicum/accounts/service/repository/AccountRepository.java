package ru.yandex.practicum.accounts.service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.service.entity.AccountEntity;

import java.util.UUID;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, UUID> {
    Flux<AccountEntity> findByUserId(UUID userId);

    Mono<AccountEntity> findFirstByUserIdAndCurrency(UUID userId, String currency);
}
