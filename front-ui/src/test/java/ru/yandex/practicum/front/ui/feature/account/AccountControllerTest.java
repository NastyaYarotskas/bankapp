package ru.yandex.practicum.front.ui.feature.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.TestSecurityConfig;
import ru.yandex.practicum.front.ui.feature.account.model.Account;
import ru.yandex.practicum.front.ui.feature.account.model.Currency;
import ru.yandex.practicum.front.ui.feature.account.model.User;
import ru.yandex.practicum.front.ui.feature.account.request.EditPasswordRequest;
import ru.yandex.practicum.front.ui.feature.account.request.UserUpdateRequest;
import ru.yandex.practicum.front.ui.feature.auth.CustomUserDetails;
import org.springframework.ui.Model;
import ru.yandex.practicum.front.ui.feature.error.ErrorResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class AccountControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private AccountsServiceClient accountsServiceClient;
    @MockitoBean
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserModelAttributes userModelAttributes;

    @Test
    void getAccountsInfo_whenUserNotAuthenticated_shouldReturnMainView() {
        webTestClient.get()
                .uri("/main")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void getAccountsInfo_whenUserAuthenticated_shouldReturnMainViewWithUserData() {
        String login = "testUser";
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        User user = User.builder()
                .login(login)
                .name("Test User")
                .build();

        List<User> allUsers = List.of(
                user,
                User.builder().login("user2").name("User 2").build()
        );

        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(user));
        when(accountsServiceClient.getAllUsers())
                .thenReturn(Flux.fromIterable(allUsers));
        doNothing().when(userModelAttributes).populateUserAttributes(any(Model.class), eq(user));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .get()
                .uri("/main")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);

        verify(accountsServiceClient).getAccountDetails(login);
        verify(accountsServiceClient).getAllUsers();
        verify(userModelAttributes).populateUserAttributes(any(Model.class), eq(user));
    }

    @Test
    void editPassword_allParamsAreValid_shouldRedirectToMain() {
        String login = "testUser";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .login(login)
                .password("newPassword")
                .confirmPassword("newPassword")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());
        User updatedUser = User.builder()
                .login(login)
                .password("newPassword")
                .build();

        when(accountsServiceClient.editPassword(eq(login), any(EditPasswordRequest.class)))
                .thenReturn(Mono.just(updatedUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("password=" + request.getPassword() + "&confirmPassword=" + request.getConfirmPassword())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");

        verify(accountsServiceClient).editPassword(login, request);
    }

    @SneakyThrows
    @Test
    void editPassword_whenError_shouldHandleErrorAndReturnToMainView() {
        String login = "testUser";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .login(login)
                .password("newPassword")
                .confirmPassword("newPassword")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        ErrorResponse errorResponse = new ErrorResponse(200, "Ошибка изменения пароля", "api/path", OffsetDateTime.now().toInstant());
        WebClientResponseException exception = WebClientResponseException
                .create(400, "Bad Request", HttpHeaders.EMPTY,
                        "{\"error\":\"Ошибка изменения пароля\"}".getBytes(), null);

        when(accountsServiceClient.editPassword(login, request))
                .thenReturn(Mono.error(exception));
        when(objectMapper.readValue(anyString(), eq(ErrorResponse.class)))
                .thenReturn(errorResponse);
        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(User.builder().login(login).build()));
        when(accountsServiceClient.getAllUsers())
                .thenReturn(Flux.just(User.builder().login(login).build()));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("password=" + request.getPassword() + "&confirmPassword=" + request.getConfirmPassword())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);

        verify(accountsServiceClient).editPassword(login, request);
        verify(objectMapper).readValue(anyString(), eq(ErrorResponse.class));
    }

    @Test
    void editPassword_whenNotAuthenticated_shouldReturnUnauthorized() {
        String login = "testUser";
        EditPasswordRequest request = EditPasswordRequest.builder()
                .login(login)
                .password("newPassword")
                .confirmPassword("newPassword")
                .build();

        webTestClient
                .post()
                .uri("/user/{login}/editPassword", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("password=" + request.getPassword() + "&confirmPassword=" + request.getConfirmPassword())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");;

        verify(accountsServiceClient, never()).editPassword(anyString(), any());
    }

    @Test
    void editUserAccounts_allParamsAreValid_shouldRedirectToMain() {
        // Подготовка тестовых данных
        String login = "testUser";
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Новое Имя");
        request.setBirthdate("1990-01-01");
        request.setAccount(List.of("RUB", "USD"));

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name("Старое Имя")
                .password("password")
                .accounts(List.of(
                        Account.builder().currency(new Currency("RUB", "RUB", 1)).exists(true).build(),
                        Account.builder().currency(new Currency("USD", "USD", 1)).exists(false).build()
                ))
                .build();

        User updatedUser = User.builder()
                .id(existingUser.getId())
                .login(login)
                .name(request.getName())
                .password(existingUser.getPassword())
                .accounts(List.of(
                        Account.builder().currency(new Currency("RUB", "RUB", 1)).exists(true).build(),
                        Account.builder().currency(new Currency("USD", "USD", 1)).exists(true).build()
                ))
                .build();

        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(existingUser));
        when(accountsServiceClient.editUserAccounts(eq(login), any(User.class)))
                .thenReturn(Mono.just(updatedUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=" + request.getName() +
                           "&birthdate=" + request.getBirthdate() +
                           "&account=RUB&account=USD")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main");

        verify(accountsServiceClient).getAccountDetails(login);
        verify(accountsServiceClient).editUserAccounts(eq(login), any(User.class));
    }

    @SneakyThrows
    @Test
    void editUserAccounts_whenError_shouldHandleErrorAndReturnToMainView() {
        String login = "testUser";
        CustomUserDetails userDetails = new CustomUserDetails(login, "password", true, true, true, true, List.of());

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Новое Имя");
        request.setBirthdate("1990-01-01");
        request.setAccount(List.of("RUB"));

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .login(login)
                .name("Старое Имя")
                .accounts(List.of())
                .build();

        ErrorResponse errorResponse = new ErrorResponse(400, "Ошибка обновления аккаунта", "api/path", OffsetDateTime.now().toInstant());
        WebClientResponseException exception = WebClientResponseException.create(
                400,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"Ошибка обновления аккаунта\"}".getBytes(),
                null
        );

        when(accountsServiceClient.getAccountDetails(login))
                .thenReturn(Mono.just(existingUser));
        when(accountsServiceClient.editUserAccounts(eq(login), any(User.class)))
                .thenReturn(Mono.error(exception));
        when(objectMapper.readValue(anyString(), eq(ErrorResponse.class)))
                .thenReturn(errorResponse);
        when(accountsServiceClient.getAllUsers())
                .thenReturn(Flux.just(existingUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
                .post()
                .uri("/user/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=" + request.getName() +
                           "&birthdate=" + request.getBirthdate() +
                           "&account=RUB")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void editUserAccounts_whenNotAuthenticated_shouldRedirectToLogin() {
        String login = "testUser";
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Новое Имя");
        request.setBirthdate("1990-01-01");
        request.setAccount(List.of("RUB"));

        webTestClient
                .post()
                .uri("/user/{login}/editUserAccounts", login)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=" + request.getName() +
                           "&birthdate=" + request.getBirthdate() +
                           "&account=RUB")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        verify(accountsServiceClient, never()).getAccountDetails(anyString());
        verify(accountsServiceClient, never()).editUserAccounts(anyString(), any());
    }
}
