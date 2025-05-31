package ru.yandex.practicum.accounts.service.feature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.service.BaseTest;
import ru.yandex.practicum.accounts.service.feature.account.Account;
import ru.yandex.practicum.accounts.service.feature.currency.Currency;
import ru.yandex.practicum.accounts.service.feature.currency.CurrencyEnum;
import ru.yandex.practicum.accounts.service.feature.user.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.CONFIRMATION_ERROR_MSG;
import static ru.yandex.practicum.accounts.service.feature.user.UserValidationErrorMessages.USER_NOT_FOUND_ERROR_MSG;

@SpringBootTest
@AutoConfigureWebTestClient
public class UserControllerTest extends BaseTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private UserService userService;

    @Test
    void createUser_validRequest_shouldReturnCreatedUser() {
        String login = "test_user";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);

        UserCreateRequest request = UserCreateRequest.builder()
                .login(login)
                .name(name)
                .password("password123")
                .confirmPassword("password123")
                .birthdate(birthDate.toString())
                .build();

        List<Account> accounts = Arrays.stream(CurrencyEnum.values())
                .map(currency -> Account.builder()
                        .id(UUID.randomUUID())
                        .currency(new Currency(currency.getTitle(), currency.name()))
                        .value(0)
                        .exists(false)
                        .build())
                .toList();

        User expectedUser = User.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name(name)
                .birthdate(birthDate.atStartOfDay().atOffset(ZoneOffset.UTC))
                .accounts(accounts)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(Mono.just(expectedUser));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.name").isEqualTo(name)
                .jsonPath("$.accounts").isArray()
                .jsonPath("$.accounts.length()").isEqualTo(CurrencyEnum.values().length);
    }

    @Test
    void createUser_invalidRequest_shouldReturnBadRequest() {
        UserCreateRequest request = UserCreateRequest.builder()
                .login("")
                .name("Test User")
                .password("password123")
                .confirmPassword("password123")
                .birthdate(LocalDate.of(1990, 1, 1).toString())
                .build();

        when(userService.createUser(any()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин не может быть пустым")));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createUser_serviceUnavailable_shouldReturnInternalServerError() {
        UserCreateRequest request = UserCreateRequest.builder()
                .login("testuser")
                .name("Test User")
                .password("password123")
                .confirmPassword("password123")
                .birthdate(LocalDate.of(1990, 1, 1).toString())
                .build();

        when(userService.createUser(any()))
                .thenReturn(Mono.error(new RuntimeException("Внутренняя ошибка сервера")));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getUser_userExists_shouldReturnUser() {
        String login = "test_user";
        User expectedUser = User.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name("Test User")
                .birthdate(OffsetDateTime.now().minusYears(20))
                .accounts(List.of())
                .build();

        when(userService.getUserByLogin(login))
                .thenReturn(Mono.just(expectedUser));

        webTestClient.get()
                .uri("/api/v1/users/{login}", login)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.name").isEqualTo("Test User")
                .jsonPath("$.id").exists()
                .jsonPath("$.birthdate").exists();
    }

    @Test
    void getUser_userNotFound_shouldReturnNotFound() {
        String nonExistentLogin = "non_existent_user";

        when(userService.getUserByLogin(nonExistentLogin))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        USER_NOT_FOUND_ERROR_MSG.formatted(nonExistentLogin))));

        webTestClient.get()
                .uri("/api/v1/users/{login}", nonExistentLogin)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllUsers_usersExist_shouldReturnAllUsers() {
        UserEntity user1 = UserEntity.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .name("User One")
                .password("password1")
                .birthdate(OffsetDateTime.now().minusYears(25))
                .build();

        UserEntity user2 = UserEntity.builder()
                .id(UUID.randomUUID())
                .login("user2")
                .name("User Two")
                .password("password2")
                .birthdate(OffsetDateTime.now().minusYears(30))
                .build();

        when(userService.getAllUsers()).thenReturn(Flux.just(user1, user2));

        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserEntity.class)
                .hasSize(2);
    }

    @Test
    void getAllUsers_emptyList_shouldReturnEmptyArray() {
        when(userService.getAllUsers()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserEntity.class)
                .hasSize(0);
    }

    @Test
    void editPassword_validRequest_shouldReturnUpdatedUser() {
        String login = "test_user";
        String newPassword = "new_password";

        EditPasswordRequest request = EditPasswordRequest.builder()
                .password(newPassword)
                .confirmPassword(newPassword)
                .build();

        User expectedUser = User.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name("Test User")
                .password(newPassword)
                .birthdate(OffsetDateTime.now().minusYears(25))
                .accounts(List.of())
                .build();

        when(userService.updateUserPassword(login, request))
                .thenReturn(Mono.just(expectedUser));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.name").isEqualTo("Test User")
                .jsonPath("$.id").exists()
                .jsonPath("$.birthdate").exists();
    }

    @Test
    void editPassword_userNotFound_shouldReturnNotFound() {
        String nonExistentLogin = "non_existent_user";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .password("new_password")
                .confirmPassword("new_password")
                .build();

        when(userService.updateUserPassword(nonExistentLogin, request))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        USER_NOT_FOUND_ERROR_MSG.formatted(nonExistentLogin))));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editPassword", nonExistentLogin)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void editPassword_passwordsDoNotMatch_shouldReturnBadRequest() {
        String login = "test_user";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .password("new_password")
                .confirmPassword("different_password")
                .build();

        when(userService.updateUserPassword(login, request))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, CONFIRMATION_ERROR_MSG)));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void editPassword_serviceUnavailable_shouldReturnInternalServerError() {
        String login = "test_user";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .password("new_password")
                .confirmPassword("new_password")
                .build();

        when(userService.updateUserPassword(login, request))
                .thenReturn(Mono.error(new RuntimeException("Внутренняя ошибка сервера")));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void editUserAccounts_validData_shouldReturnUpdatedUser() {
        String login = "test_login";
        List<Account> accounts = Arrays.stream(CurrencyEnum.values())
                .map(currency -> Account.builder()
                        .currency(new Currency(currency.getTitle(), currency.name()))
                        .value(0)
                        .exists(false)
                        .build())
                .toList();

        User user = User.builder()
                .login(login)
                .name("Test User")
                .accounts(accounts)
                .build();

        when(userService.updateUserAccounts(eq(login), any(User.class)))
                .thenReturn(Mono.just(user));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.name").isEqualTo("Test User")
                .jsonPath("$.accounts").isArray()
                .jsonPath("$.accounts.length()").isEqualTo(CurrencyEnum.values().length);
    }

    @Test
    void editUserAccounts_userNotFound_shouldReturnNotFound() {
        String nonExistentLogin = "non_existent_user";
        User user = User.builder()
                .login(nonExistentLogin)
                .name("Test User")
                .accounts(List.of())
                .build();

        when(userService.updateUserAccounts(eq(nonExistentLogin), any(User.class)))
                .thenReturn(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        USER_NOT_FOUND_ERROR_MSG.formatted(nonExistentLogin))
                ));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editUserAccounts", nonExistentLogin)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void editUserAccounts_invalidRequest_shouldReturnBadRequest() {
        String login = "test_login";
        User invalidUser = User.builder().build();

        when(userService.updateUserAccounts(eq(login), any(User.class)))
                .thenReturn(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Некорректные данные пользователя"
                )));

        webTestClient.post()
                .uri("/api/v1/users/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUser)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
