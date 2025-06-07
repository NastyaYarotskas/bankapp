package ru.yandex.practicum.transfer.service.feature.transfer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestWebClientConfig {

    @Bean
    @Primary
    @Qualifier("notificationServiceWebClient")
    public WebClient testNotificationServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9001")
                .build();
    }

    @Bean
    @Primary
    @Qualifier("blockerServiceWebClient")
    public WebClient testBlockerServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9002")
                .build();
    }

    @Bean
    @Primary
    @Qualifier("accountsServiceWebClient")
    public WebClient testAccountsServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9003")
                .build();
    }

    @Bean
    @Primary
    @Qualifier("exchangeServiceWebClient")
    public WebClient testExchangeServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9004")
                .build();
    }
}
