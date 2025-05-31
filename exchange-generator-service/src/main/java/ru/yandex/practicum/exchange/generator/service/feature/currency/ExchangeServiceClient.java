package ru.yandex.practicum.exchange.generator.service.feature.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ExchangeServiceClient {

    @Autowired
    private WebClient exchangeServiceWebClient;

    public Mono<Currency> updateCurrencyRate(String code, Mono<Currency> updatedCurrency) {
        return exchangeServiceWebClient.put()
                .uri("/api/currencies/" + code)
                .body(updatedCurrency, Currency.class)
                .retrieve()
                .bodyToMono(Currency.class);
    }
}
