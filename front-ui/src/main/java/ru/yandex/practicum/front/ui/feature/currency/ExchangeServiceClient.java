package ru.yandex.practicum.front.ui.feature.currency;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.front.ui.feature.account.model.Currency;

@Component
public class ExchangeServiceClient {

    private final WebClient exchangeServiceWebClient;

    public ExchangeServiceClient(@LoadBalanced WebClient.Builder exchangeServiceWebClientBuilder) {
        this.exchangeServiceWebClient = exchangeServiceWebClientBuilder.build();
    }

    public Flux<Currency> getCurrencyRates() {
        return exchangeServiceWebClient.get()
                .uri("/api/currencies")
                .retrieve()
                .bodyToFlux(Currency.class);
    }
}
