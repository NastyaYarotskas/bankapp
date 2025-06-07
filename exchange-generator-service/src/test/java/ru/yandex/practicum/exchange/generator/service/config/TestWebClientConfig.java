package ru.yandex.practicum.exchange.generator.service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestWebClientConfig {

    @Bean
    @Primary
    public WebClient testExchangeServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9001")
                .build();
    }
}
