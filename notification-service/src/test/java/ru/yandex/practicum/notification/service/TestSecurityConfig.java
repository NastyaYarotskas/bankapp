package ru.yandex.practicum.notification.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        // Mock JWT decoder for tests
        return token -> {
            // Customize claims as needed for your tests
            return Jwt.withTokenValue("mock-token")
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .claim("scope", "openid")
                    .build();
        };
    }
}
