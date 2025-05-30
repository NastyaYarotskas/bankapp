package ru.yandex.practicum.exchange.generator.service.feature.currency;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ExchangeServiceClient {

    private final WebClient exchangeServiceWebClient;

    public ExchangeServiceClient(@LoadBalanced WebClient.Builder exchangeServiceWebClientBuilder) {
        this.exchangeServiceWebClient = exchangeServiceWebClientBuilder.build();
    }

    public Mono<Currency> updateCurrencyRate(String code, Mono<Currency> updatedCurrency) {
        return exchangeServiceWebClient.put()
                .uri("/api/currencies/" + code)
                .body(updatedCurrency, Currency.class)
                .retrieve()
                .bodyToMono(Currency.class);
    }
}
