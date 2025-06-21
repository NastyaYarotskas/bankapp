package ru.yandex.practicum.front.ui.config;

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
//    @LoadBalanced
    public WebClient.Builder cashServiceWebClientBuilder(@Value("${cash.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
    public WebClient.Builder exchangeServiceWebClientBuilder(@Value("${exchange.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }

    @Bean
//    @LoadBalanced
    public WebClient.Builder transferServiceWebClientBuilder(@Value("${transfer.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl);
    }
}
