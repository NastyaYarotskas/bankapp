package ru.yandex.practicum.accounts.service.feature;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;
import ru.yandex.practicum.accounts.service.BaseTest;
import ru.yandex.practicum.accounts.service.feature.account.Account;
import ru.yandex.practicum.accounts.service.feature.currency.Currency;
import ru.yandex.practicum.accounts.service.feature.currency.CurrencyEnum;
import ru.yandex.practicum.accounts.service.feature.notification.NotificationRequest;
import ru.yandex.practicum.accounts.service.feature.user.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.*;

@AutoConfigureStubRunner(
        ids = "ru.yandex.practicum:notification-service:+:stubs:9001",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import(TestConfig.class)
public class UserServiceTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    void createUser_allParamsAreValid_shouldCreateUser() {
        LocalDate birthDate = LocalDate.of(1998, 10, 10);
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .login("test_user")
                .password("test_password")
                .confirmPassword("test_password")
                .name("test_name")
                .birthdate(birthDate.toString())
                .build();

        List<Account> expectedAccounts = Arrays.stream(CurrencyEnum.values())
                .map(currency -> Account.builder()
                        .currency(new Currency(currency.getTitle(), currency.name()))
                        .value(0)
                        .exists(false)
                        .build())
                .toList();

        User expectedUser = User.builder()
                .login("test_user")
                .name("test_name")
                .birthdate(birthDate.atStartOfDay().atOffset(ZoneOffset.UTC))
                .accounts(expectedAccounts)
                .build();

        StepVerifier.create(userService.createUser(userCreateRequest))
                .assertNext(actualUser -> {
                    Assertions.assertThat(actualUser)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "accounts", "password")
                            .isEqualTo(expectedUser);

                    Assertions.assertThat(actualUser.getAccounts())
                            .usingRecursiveComparison()
                            .ignoringFields("id")
                            .ignoringCollectionOrder()
                            .isEqualTo(expectedUser.getAccounts());
                })
                .verifyComplete();
    }

    @Test
    void createUser_passwordAndConfirmedPasswordNotEqual_shouldReturnBadRequest() {
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .login("test_user")
                .password("test_password")
                .confirmPassword("wrong_password")
                .name("test_name")
                .birthdate("1998-10-10")
                .build();

        StepVerifier.create(userService.createUser(userCreateRequest))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(CONFIRMATION_ERROR_MSG);
                })
                .verify();

        StepVerifier.create(userRepository.findByLogin("test_user"))
                .expectComplete()
                .verify();
    }

    @Test
    void createUser_invalidBirthdayFormat_shouldReturnBadRequest() {
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .login("test_user")
                .password("test_password")
                .confirmPassword("test_password")
                .name("test_name")
                .birthdate("invalid-date")
                .build();

        StepVerifier.create(userService.createUser(userCreateRequest))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(INVALID_BIRTHDAY_FORMAT_ERROR_MSG);
                })
                .verify();

        StepVerifier.create(userRepository.findByLogin("test_user"))
                .expectComplete()
                .verify();
    }

    @Test
    void createUser_invalidBirthdayValue_shouldReturnBadRequest() {
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .login("test_user")
                .password("test_password")
                .confirmPassword("test_password")
                .name("test_name")
                .birthdate(LocalDate.now().toString())
                .build();

        StepVerifier.create(userService.createUser(userCreateRequest))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(INVALID_BIRTHDAY_ERROR_MSG);
                })
                .verify();

        StepVerifier.create(userRepository.findByLogin("test_user"))
                .expectComplete()
                .verify();
    }

    @Test
    void getUserByLogin_userExists_shouldReturnUser() {
        LocalDate birthDate = LocalDate.of(1998, 10, 10);
        ZoneOffset expectedOffset = ZoneOffset.UTC;

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .login("test_user")
                .password("test_password")
                .confirmPassword("test_password")
                .name("test_name")
                .birthdate(birthDate.toString())
                .build();

        List<Account> expectedAccounts = Arrays.stream(CurrencyEnum.values())
                .map(currency -> Account.builder()
                        .currency(new Currency(currency.getTitle(), currency.name()))
                        .value(0)
                        .exists(false)
                        .build())
                .toList();

        User expectedUser = User.builder()
                .login("test_user")
                .name("test_name")
                .password("test_password")
                .birthdate(birthDate.atStartOfDay().atOffset(expectedOffset))
                .accounts(expectedAccounts)
                .build();

        StepVerifier.create(userService.createUser(userCreateRequest))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(userService.getUserByLogin("test_user"))
                .assertNext(actualUser -> {
                    Assertions.assertThat(actualUser.getBirthdate())
                            .isEqualTo(birthDate.atStartOfDay().atOffset(expectedOffset));

                    Assertions.assertThat(actualUser)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "accounts", "password", "birthdate")
                            .isEqualTo(expectedUser);

                    Assertions.assertThat(actualUser.getAccounts())
                            .usingRecursiveComparison()
                            .ignoringFields("id")
                            .ignoringCollectionOrder()
                            .isEqualTo(expectedUser.getAccounts());
                })
                .verifyComplete();
    }

    @Test
    void getUserByLogin_userDoesNotExist_shouldReturnNotFound() {
        String nonExistentLogin = "non_existent_user";

        StepVerifier.create(userService.getUserByLogin(nonExistentLogin))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(USER_NOT_FOUND_ERROR_MSG.formatted(nonExistentLogin));
                })
                .verify();
    }

    @Test
    void getAllUsers_usersExist_shouldReturnAllUsers() {
        UserCreateRequest firstUser = UserCreateRequest.builder()
                .login("first_user")
                .password("password1")
                .confirmPassword("password1")
                .name("First User")
                .birthdate("1990-01-01")
                .build();

        UserCreateRequest secondUser = UserCreateRequest.builder()
                .login("second_user")
                .password("password2")
                .confirmPassword("password2")
                .name("Second User")
                .birthdate("1995-02-02")
                .build();

        StepVerifier.create(userService.createUser(firstUser)
                        .then(userService.createUser(secondUser))
                        .thenMany(userService.getAllUsers()))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(userService.getAllUsers())
                .assertNext(user -> {
                    Assertions.assertThat(user.getLogin()).isIn("first_user", "second_user");
                    Assertions.assertThat(user.getName()).isIn("First User", "Second User");
                })
                .assertNext(user -> {
                    Assertions.assertThat(user.getLogin()).isIn("first_user", "second_user");
                    Assertions.assertThat(user.getName()).isIn("First User", "Second User");
                })
                .verifyComplete();
    }

    @Test
    void updateUserPassword_passwordIsValid_shouldUpdatePassword() {
        String login = "test_user";
        String oldPassword = "old_password";
        String newPassword = "new_password";
        String expectedNotificationMessage = "Пароль успешно обновлен";

        NotificationRequest expectedRequest = new NotificationRequest(login, expectedNotificationMessage);

        UserCreateRequest createRequest = UserCreateRequest.builder()
                .login(login)
                .password(oldPassword)
                .confirmPassword(oldPassword)
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        EditPasswordRequest editRequest = EditPasswordRequest.builder()
                .password(newPassword)
                .confirmPassword(newPassword)
                .build();

        StepVerifier.create(userService.createUser(createRequest)
                        .then(userService.updateUserPassword(login, editRequest)))
                .assertNext(updatedUser -> {
                    Assertions.assertThat(updatedUser.getLogin()).isEqualTo(login);
                    Assertions.assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
                })
                .verifyComplete();

        StepVerifier.create(userRepository.findByLogin(login))
                .assertNext(userEntity ->
                        Assertions.assertThat(userEntity.getPassword()).isEqualTo(newPassword)
                )
                .verifyComplete();
    }

    @Test
    void updateUserPassword_passwordIsNotConfirmed_shouldReturnBadRequest() {
        String login = "test_user";
        String originalPassword = "original_password";
        String newPassword = "new_password";
        String wrongConfirmPassword = "wrong_password";
        String expectedErrorMessage = "Не удалось обновить пароль. Причина: 400 BAD_REQUEST \"Пароль и подтверждение пароля не совпадают\"";

        NotificationRequest expectedRequest = new NotificationRequest(login, expectedErrorMessage);

        UserCreateRequest createRequest = UserCreateRequest.builder()
                .login(login)
                .password(originalPassword)
                .confirmPassword(originalPassword)
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        EditPasswordRequest editRequest = EditPasswordRequest.builder()
                .password(newPassword)
                .confirmPassword(wrongConfirmPassword)
                .build();

        StepVerifier.create(userService.createUser(createRequest)
                        .then(userService.updateUserPassword(login, editRequest)))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(CONFIRMATION_ERROR_MSG);
                })
                .verify();

        StepVerifier.create(userRepository.findByLogin(login))
                .assertNext(userEntity ->
                        Assertions.assertThat(userEntity.getPassword()).isEqualTo(originalPassword)
                )
                .verifyComplete();
    }

    @Test
    void updateUserAccounts_validData_shouldUpdateUserAndAccounts() throws IOException {
        String login = "test_user";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);

        UserCreateRequest createRequest = UserCreateRequest.builder()
                .login(login)
                .password("password")
                .confirmPassword("password")
                .name(name)
                .birthdate(birthDate.toString())
                .build();

        String updatedName = "Updated User";
        LocalDate updatedBirthDate = LocalDate.of(1991, 2, 2);
        List<Account> updatedAccounts = List.of(
                Account.builder()
                        .currency(new Currency(CurrencyEnum.RUB.getTitle(), CurrencyEnum.RUB.name()))
                        .value(1000)
                        .exists(true)
                        .build(),
                Account.builder()
                        .currency(new Currency(CurrencyEnum.USD.getTitle(), CurrencyEnum.USD.name()))
                        .value(100)
                        .exists(true)
                        .build(),
                Account.builder()
                        .currency(new Currency(CurrencyEnum.CNY.getTitle(), CurrencyEnum.CNY.name()))
                        .value(0)
                        .exists(false)
                        .build()
        );

        User updateRequest = User.builder()
                .name(updatedName)
                .birthdate(updatedBirthDate.atStartOfDay().atOffset(ZoneOffset.UTC))
                .accounts(updatedAccounts)
                .build();

        StepVerifier.create(
                        userService.createUser(createRequest)
                                .flatMap(createdUser -> {
                                    for (int i = 0; i < updatedAccounts.size(); i++) {
                                        updatedAccounts.get(i).setId(createdUser.getAccounts().get(i).getId());
                                    }
                                    return userService.updateUserAccounts(login, updateRequest);
                                })
                )
                .assertNext(updatedUser -> {
                    Assertions.assertThat(updatedUser.getName()).isEqualTo(updatedName);
                    Assertions.assertThat(updatedUser.getBirthdate())
                            .isEqualTo(updatedBirthDate.atStartOfDay().atOffset(ZoneOffset.UTC));

                    Assertions.assertThat(updatedUser.getAccounts())
                            .usingRecursiveComparison()
                            .ignoringFields("id")
                            .ignoringCollectionOrder()
                            .isEqualTo(updatedAccounts);
                })
                .verifyComplete();
    }

    @Test
    void updateUserAccounts_userNotFound_shouldReturnNotFound() {
        String nonExistentLogin = "non_existent_user";
        User updateRequest = User.builder()
                .name("Test")
                .birthdate(LocalDate.of(1990, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .accounts(List.of())
                .build();

        StepVerifier.create(userService.updateUserAccounts(nonExistentLogin, updateRequest))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(USER_NOT_FOUND_ERROR_MSG.formatted(nonExistentLogin), nonExistentLogin);
                })
                .verify();
    }

    @Test
    void updateUserAccounts_invalidBirthdate_shouldReturnBadRequest() {
        String login = "test_user";
        UserCreateRequest createRequest = UserCreateRequest.builder()
                .login(login)
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        User updateRequest = User.builder()
                .name("Updated User")
                .birthdate(LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC))
                .accounts(List.of())
                .build();

        StepVerifier.create(userService.createUser(createRequest)
                        .then(userService.updateUserAccounts(login, updateRequest)))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(INVALID_BIRTHDAY_ERROR_MSG);
                })
                .verify();
    }

    @Test
    void updateUserAccounts_invalidAccounts_shouldReturnBadRequest() {
        String login = "test_user";
        UserCreateRequest createRequest = UserCreateRequest.builder()
                .login(login)
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        List<Account> invalidAccounts = List.of(
                Account.builder()
                        .currency(new Currency("Invalid", "INVALID"))
                        .value(1000)
                        .exists(true)
                        .build()
        );

        User updateRequest = User.builder()
                .name("Updated User")
                .birthdate(LocalDate.of(1990, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .accounts(invalidAccounts)
                .build();

        StepVerifier.create(userService.createUser(createRequest)
                        .then(userService.updateUserAccounts(login, updateRequest)))
                .expectErrorSatisfies(error -> {
                    Assertions.assertThat(error)
                            .isInstanceOf(ResponseStatusException.class);
                    Assertions.assertThat(error.getMessage())
                            .contains(INVALID_ACCOUNT_LIST_ERROR_MSG);
                })
                .verify();
    }
}
