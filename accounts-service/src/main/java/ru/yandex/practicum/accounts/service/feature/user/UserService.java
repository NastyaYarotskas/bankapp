package ru.yandex.practicum.accounts.service.feature.user;

import lombok.RequiredArgsConstructor;
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
import ru.yandex.practicum.accounts.service.feature.currency.Currency;
import ru.yandex.practicum.accounts.service.feature.currency.CurrencyEnum;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AccountService accountService;
    private final UserRepository userRepository;

    public Mono<User> createUser(UserCreateRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "Пароль и подтверждение пароля не совпадают"));
        }

        if (request.getBirthdate() != null) {
            LocalDate birthdate = LocalDate.parse(request.getBirthdate()).atStartOfDay().atOffset(ZoneOffset.UTC).toLocalDate();

            LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);

            if (birthdate.isAfter(eighteenYearsAgo)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Пользователь должен быть старше 18 лет"));
            }
        } else {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата рождения должна быть заполнена"));
        }

        return userRepository.findByLogin(request.getLogin())
                .flatMap(existingUser -> Mono.<User>error(
                        new ResponseStatusException(HttpStatusCode.valueOf(400),
                                "Пользователь с логином " + request.getLogin() + " уже существует")
                ))
                .switchIfEmpty(
                        createUserEntity(request)
                                .flatMap(userEntity -> createUserAccounts(userEntity.getId())
                                        .map(accounts -> Tuples.of(userEntity, accounts)))
                                .map(this::mapToUser)
                );
    }

    public Mono<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .flatMap(userEntity -> accountService.findByUserId(userEntity.getId())
                        .collectList()
                        .map(accountEntities -> mapToUser(Tuples.of(userEntity, accountEntities))))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(404), "Пользователь с логином " + login + " не найден")));
    }

    public Flux<UserEntity> getAllUsers() {
        return userRepository.findAll();
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
                        .userId(userId)  // Добавляем userId
                        .build())
                .toList();

        return accountService.createAccounts(Flux.fromIterable(accountEntities))
                .collectList();
    }

    private User mapToUser(Tuple2<UserEntity, List<AccountEntity>> tuple) {
        UserEntity userEntity = tuple.getT1();
        List<Account> accounts = tuple.getT2().stream()
                .map(this::mapToAccount)
                .toList();

        return User.builder()
                .id(userEntity.getId())
                .login(userEntity.getLogin())
                .name(userEntity.getName())
                .password(userEntity.getPassword())
                .birthdate(userEntity.getBirthdate())
                .accounts(accounts)
                .build();
    }

    private Account mapToAccount(AccountEntity accountEntity) {
        CurrencyEnum currencyEnum = CurrencyEnum.valueOf(accountEntity.getCurrency());
        Account account = Account.builder()
                .currency(new Currency(currencyEnum.getTitle(), currencyEnum.name()))
                .value(accountEntity.getValue())
                .exists(accountEntity.isExists())
                .build();
        account.setId(accountEntity.getId());
        return account;
    }

    public Mono<User> updateUserPassword(String login, EditPasswordRequest editPasswordRequest) {
        if (!editPasswordRequest.getPassword().equals(editPasswordRequest.getConfirmPassword())) {
            return Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "Пароль и подтверждение пароля не совпадают"));
        }

        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Пользователь с логином " + login + " не найден")))
                .flatMap(userEntity -> {
                    userEntity.setPassword(editPasswordRequest.getPassword());
                    return userRepository.save(userEntity);
                })
                .flatMap(savedUser -> accountService.findByUserId(savedUser.getId())
                        .collectList()
                        .map(accounts -> Tuples.of(savedUser, accounts)))
                .map(this::mapToUser);
    }

    public Mono<User> updateUserAccounts(String login, User user) {
        if (!login.equals(user.getLogin())) {
            return Mono.error(new RuntimeException("Логин в URL не совпадает с логином в запросе"));
        }

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
                .map(this::mapToUser);
    }
}