package ru.yandex.practicum.exchange.generator.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

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
