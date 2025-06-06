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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.TestSecurityConfig;
import ru.yandex.practicum.front.ui.feature.account.model.User;
import ru.yandex.practicum.front.ui.feature.account.request.CreateUserRequest;
import ru.yandex.practicum.front.ui.feature.error.ErrorResponse;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class SignUpControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private AccountsServiceClient accountsServiceClient;

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserModelAttributes userModelAttributes;

    @Test
    void getSignUpForm_shouldReturnSignupView() {
        webTestClient.get()
                .uri("/signup")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void registerUser_whenValidRequest_shouldRedirectToLogin() {
        CreateUserRequest request = CreateUserRequest.builder()
                .login("testUser")
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        User createdUser = User.builder()
                .login(request.getLogin())
                .name(request.getName())
                .build();

        when(accountsServiceClient.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.just(createdUser));

        webTestClient.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("login=" + request.getLogin() +
                           "&password=" + request.getPassword() +
                           "&confirmPassword=" + request.getConfirmPassword() +
                           "&name=" + request.getName() +
                           "&birthdate=" + request.getBirthdate())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/login");

        verify(accountsServiceClient).createUser(any(CreateUserRequest.class));
    }

    @SneakyThrows
    @Test
    void registerUser_whenError_shouldReturnToSignupWithError() {
        CreateUserRequest request = CreateUserRequest.builder()
                .login("testUser")
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1990-01-01")
                .build();

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Пользователь с таким логином уже существует",
                "api/path",
                OffsetDateTime.now().toInstant()
        );

        WebClientResponseException exception = WebClientResponseException.create(
                400,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"Пользователь с таким логином уже существует\"}".getBytes(),
                null
        );

        when(accountsServiceClient.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(exception));
        when(objectMapper.readValue(anyString(), eq(ErrorResponse.class)))
                .thenReturn(errorResponse);

        webTestClient.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("login=" + request.getLogin() +
                           "&password=" + request.getPassword() +
                           "&confirmPassword=" + request.getConfirmPassword() +
                           "&name=" + request.getName() +
                           "&birthdate=" + request.getBirthdate())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);

        verify(accountsServiceClient).createUser(any(CreateUserRequest.class));
        verify(objectMapper).readValue(anyString(), eq(ErrorResponse.class));
        verify(userModelAttributes).populateSignupModel(any(), any(), any());
    }
}