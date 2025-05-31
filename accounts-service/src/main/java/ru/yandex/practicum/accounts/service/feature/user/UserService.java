package ru.yandex.practicum.accounts.service.feature.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.accounts.service.feature.account.Account;
import ru.yandex.practicum.accounts.service.feature.account.AccountEntity;
import ru.yandex.practicum.accounts.service.feature.account.AccountService;
import ru.yandex.practicum.accounts.service.feature.currency.CurrencyEnum;
import ru.yandex.practicum.accounts.service.feature.notification.NotificationRequest;
import ru.yandex.practicum.accounts.service.feature.notification.NotificationServiceClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.*;
import static ru.yandex.practicum.accounts.service.feature.user.UserValidator.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final NotificationServiceClient notificationServiceClient;

    public Mono<User> createUser(UserCreateRequest request) {
        return validateRequest(request)
                .then(validateExistingUser(request.getLogin()))
                .then(Mono.just(request))
                .flatMap(this::createUserWithAccounts);
    }

    private Mono<Void> validateRequest(UserCreateRequest request) {
        return Mono.just(request)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, EMPTY_REQUEST_ERROR_MSG)))
                .flatMap(r -> validatePasswordChange(r.getPassword(), r.getConfirmPassword())
                        .then(Mono.just(r)))
                .flatMap(r -> validateBirthdate(r.getBirthdate())
                        .then(Mono.just(r)))
                .then();
    }

    private Mono<Void> validateExistingUser(String login) {
        return userRepository.findByLogin(login)
                .hasElement()
                .flatMap(exists -> exists
                        ? Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, LOING_ERROR_MSG.formatted(login)))
                        : Mono.empty());
    }

    private Mono<User> createUserWithAccounts(UserCreateRequest request) {
        return createUserEntity(request)
                .flatMap(userEntity -> createUserAccounts(userEntity.getId())
                        .map(accounts -> Tuples.of(userEntity, accounts)))
                .map(tuple -> UserMapper.toUser(tuple.getT1(), tuple.getT2()));
    }

    private Mono<UserEntity> createUserEntity(UserCreateRequest request) {
        OffsetDateTime birthdate = LocalDate.parse(request.getBirthdate()).atStartOfDay().atOffset(ZoneOffset.UTC);

        return Mono.just(UserEntity.builder()
                        .login(request.getLogin())
                        .name(request.getName())
                        .password(request.getPassword())
                        .birthdate(birthdate)
                        .build())
                .flatMap(userRepository::save);
    }

    private Mono<List<AccountEntity>> createUserAccounts(UUID userId) {
        List<AccountEntity> accountEntities = Arrays.stream(CurrencyEnum.values())
                .map(currency -> AccountEntity.builder()
                        .currency(currency.name())
                        .userId(userId)
                        .build())
                .toList();

        return accountService.createAccounts(Flux.fromIterable(accountEntities))
                .collectList();
    }

    public Mono<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .flatMap(userEntity -> accountService.findByUserId(userEntity.getId())
                        .collectList()
                        .map(accountEntities -> UserMapper.toUser(userEntity, accountEntities)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(404), LOING_ERROR_MSG.formatted(login))));
    }

    public Flux<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> updateUserPassword(String login, EditPasswordRequest request) {
        return validatePasswordChange(request.getPassword(), request.getConfirmPassword())
                .then(findAndUpdateUser(login, request))
                .flatMap(this::enrichWithAccounts)
                .map(tuple -> UserMapper.toUser(tuple.getT1(), tuple.getT2()))
                .doOnSuccess(updatedUser -> {
                    String notificationMessage = "Пароль успешно обновлен";

                    notificationServiceClient.sendNotification(
                                    new NotificationRequest(login, notificationMessage)
                            )
                            .subscribe(
                                    null,
                                    error -> log.error("Failed to send update notification to user {}", login, error)
                            );
                });
    }

    private Mono<UserEntity> findAndUpdateUser(String login, EditPasswordRequest request) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR_MSG.formatted(login))))
                .flatMap(userEntity -> {
                    userEntity.setPassword(request.getPassword());
                    return userRepository.save(userEntity);
                });
    }

    private Mono<Tuple2<UserEntity, List<AccountEntity>>> enrichWithAccounts(UserEntity userEntity) {
        return accountService.findByUserId(userEntity.getId())
                .collectList()
                .map(accounts -> Tuples.of(userEntity, accounts));
    }

    public Mono<User> updateUserAccounts(String login, User user) {
        return Mono.just(user)
                .flatMap(r -> validateBirthdate(r.getBirthdate()).then(Mono.just(r)))
                .flatMap(r -> validateAccounts(r).then(Mono.just(r)))
                .then(findAndUpdateUserData(login, user))
                .flatMap(userEntity -> updateUserAccountsData(userEntity, user.getAccounts()))
                .flatMap(this::enrichWithAccounts)
                .map(tuple -> UserMapper.toUser(tuple.getT1(), tuple.getT2()))
                .doOnSuccess(updatedUser -> {
                    String notificationMessage = String.format(
                            "Данные вашего аккаунта успешно обновлены. " +
                            "Изменено счетов: %d",
                            user.getAccounts().size()
                    );

                    notificationServiceClient.sendNotification(
                                    new NotificationRequest(login, notificationMessage)
                            )
                            .subscribe(
                                    null,
                                    error -> log.error("Failed to send update notification to user {}", login, error)
                            );
                });
    }

    private Mono<UserEntity> findAndUpdateUserData(String login, User user) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR_MSG.formatted(login))))
                .flatMap(userEntity -> {
                    userEntity.setName(user.getName());
                    userEntity.setBirthdate(user.getBirthdate());
                    return userRepository.save(userEntity);
                });
    }

    private Mono<UserEntity> updateUserAccountsData(UserEntity userEntity, List<Account> userAccounts) {
        return accountService.findByUserId(userEntity.getId())
                .collectList()
                .flatMap(accounts -> {
                    List<AccountEntity> accountEntities = convertToAccountEntities(userAccounts);
                    return accountService.updateAccounts(userEntity.getId(), Flux.fromIterable(accountEntities))
                            .collectList()
                            .thenReturn(userEntity);
                });
    }

    private List<AccountEntity> convertToAccountEntities(List<Account> accounts) {
        return accounts.stream()
                .map(account -> AccountEntity.builder()
                        .id(account.getId())
                        .currency(account.getCurrency().getName())
                        .value(account.getValue())
                        .exists(account.isExists())
                        .build())
                .toList();
    }
}