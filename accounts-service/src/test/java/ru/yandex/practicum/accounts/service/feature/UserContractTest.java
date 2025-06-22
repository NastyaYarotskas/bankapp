package ru.yandex.practicum.accounts.service.feature;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.accounts.service.BaseTest;
import ru.yandex.practicum.accounts.service.request.UserCreateRequest;
import ru.yandex.practicum.accounts.service.service.UserService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.cloud.contract.stubrunner.spring.cloud.consul.StubRunnerSpringCloudConsulAutoConfiguration"
})
@AutoConfigureWebTestClient
@AutoConfigureStubRunner(
        ids = "ru.yandex.practicum:notification-service:+:stubs:8888",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({TestSecurityConfig.class, OAuth2ClientTestConfig.class})
public class UserContractTest extends BaseTest {

    @Autowired
    WebTestClient webTestClient;
    @MockitoBean
    private ReactiveOAuth2AuthorizedClientManager manager;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.webTestClient(webTestClient);

        // for getAllUsers() contract test
        UserCreateRequest firstUser = UserCreateRequest.builder()
                .login("user1")
                .password("password1")
                .confirmPassword("password1")
                .name("User One")
                .birthdate("1990-01-01")
                .build();

        UserCreateRequest secondUser = UserCreateRequest.builder()
                .login("user2")
                .password("password2")
                .confirmPassword("password2")
                .name("User Two")
                .birthdate("1995-02-02")
                .build();

        // for editPassword() contract test
        UserCreateRequest editPasswordUser = UserCreateRequest.builder()
                .login("edit_password_user")
                .password("password")
                .confirmPassword("password")
                .name("Edit Password User")
                .birthdate("1995-02-02")
                .build();

        // for editUserAccounts() contract test
        UserCreateRequest editUserAccounts = UserCreateRequest.builder()
                .login("test_edit_user_login")
                .password("password")
                .confirmPassword("password")
                .name("Test User")
                .birthdate("1995-02-02")
                .build();

        StepVerifier.create(userService.createUser(firstUser)
                        .then(userService.createUser(secondUser)
                                .then(userService.createUser(editPasswordUser))
                                .then(userService.createUser(editUserAccounts)))
                        .thenMany(userService.getAllUsers()))
                .expectNextCount(4)
                .verifyComplete();

        OAuth2AccessToken token = new OAuth2AccessToken(
                BEARER, "mock-token", Instant.now(), Instant.now().plusSeconds(300));

        when(manager.authorize(any()))
                .thenReturn(Mono.just(new OAuth2AuthorizedClient(
                        mock(ClientRegistration.class),
                        "principal",
                        token)));
    }
}
