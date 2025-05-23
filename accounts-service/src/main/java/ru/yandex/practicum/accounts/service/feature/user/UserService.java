package ru.yandex.practicum.accounts.service.feature.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import ru.yandex.practicum.accounts.service.feature.account.AccountEntity;
import ru.yandex.practicum.accounts.service.feature.account.AccountService;
import ru.yandex.practicum.accounts.service.feature.currency.CurrencyEnum;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.EMPTY_REQUEST_ERROR_MSG;
import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.LOING_ERROR_MSG;
import static ru.yandex.practicum.accounts.service.feature.user.UserValidator.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountService accountService;
    private final UserRepository userRepository;

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

    public Mono<User> updateUserPassword(String login, EditPasswordRequest editPasswordRequest) {
        if (!editPasswordRequest.getPassword().equals(editPasswordRequest.getConfirmPassword())) {
            return Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "Пароль и подтверждение пароля не совпадают"));
        }

        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(404),
                        LOING_ERROR_MSG.formatted(login))))
                .flatMap(userEntity -> {
                    userEntity.setPassword(editPasswordRequest.getPassword());
                    return userRepository.save(userEntity);
                })
                .flatMap(savedUser -> accountService.findByUserId(savedUser.getId())
                        .collectList()
                        .map(accounts -> Tuples.of(savedUser, accounts)))
                .map(tuple -> UserMapper.toUser(tuple.getT1(), tuple.getT2()));
    }

    public Mono<User> updateUserAccounts(String login, User user) {
        if (user.getBirthdate() != null) {
            LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
            LocalDate birthdate = user.getBirthdate().toLocalDate();

            if (birthdate.isAfter(eighteenYearsAgo)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь должен быть старше 18 лет"));
            }
        }

        boolean hasInvalidAccounts = user.getAccounts().stream()
                .anyMatch(account -> !account.isExists() && account.getValue() != 0);

        if (hasInvalidAccounts) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Для отключенных аккаунтов (exists=false) значение value должно быть 0"));
        }

        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new RuntimeException("Пользователь с логином " + login + " не найден")))
                .flatMap(userEntity -> {
                    userEntity.setName(user.getName());
                    userEntity.setBirthdate(user.getBirthdate());
                    return userRepository.save(userEntity);
                })
                .flatMap(userEntity -> {
                    List<AccountEntity> accountEntities = user.getAccounts().stream()
                            .map(account -> AccountEntity.builder()
                                    .id(account.getId())
                                    .currency(account.getCurrency().getName())
                                    .value(account.getValue())
                                    .exists(account.isExists())
                                    .build())
                            .toList();

                    return accountService.updateAccounts(userEntity.getId(), Flux.fromIterable(accountEntities))
                            .collectList()
                            .map(accounts -> Tuples.of(userEntity, accounts));
                })
                .map(tuple -> UserMapper.toUser(tuple.getT1(), tuple.getT2()));
    }
}