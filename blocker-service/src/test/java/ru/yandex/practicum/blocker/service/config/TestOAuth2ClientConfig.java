package ru.yandex.practicum.blocker.service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class TestOAuth2ClientConfig {

    @Bean
    @Primary
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> Mono.just(new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "test-user",
                        "scope", "openid profile",
                        "roles", List.of("USER")
                )
        ));
    }
}
