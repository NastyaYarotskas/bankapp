package ru.yandex.practicum.transfer.service.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder accountsServiceWebClientBuilder(@Value("${account.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    public WebClient accountsServiceWebClient(WebClient.Builder accountsServiceWebClientBuilder) {
        return accountsServiceWebClientBuilder.build();
    }

    @Bean
    public WebClient.Builder blockerServiceWebClientBuilder(@Value("${blocker.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    public WebClient blockerServiceWebClient(WebClient.Builder blockerServiceWebClientBuilder) {
        return blockerServiceWebClientBuilder.build();
    }

    @Bean
    public WebClient.Builder notificationServiceWebClientBuilder(@Value("${notification.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    public WebClient notificationServiceWebClient(WebClient.Builder notificationServiceWebClientBuilder) {
        return notificationServiceWebClientBuilder.build();
    }

    @Bean
    public WebClient.Builder exchangeServiceWebClientBuilder(@Value("${exchange.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    public WebClient exchangeServiceWebClient(WebClient.Builder exchangeServiceWebClientBuilder) {
        return exchangeServiceWebClientBuilder.build();
    }
}
