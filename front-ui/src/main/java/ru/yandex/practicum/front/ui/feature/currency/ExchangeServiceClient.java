package ru.yandex.practicum.front.ui.feature.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.front.ui.feature.account.model.Currency;

@Component
public class ExchangeServiceClient {

    @Autowired
    private WebClient exchangeServiceWebClient;

    public Flux<Currency> getCurrencyRates() {
        return exchangeServiceWebClient.get()
                .uri("/api/currencies")
                .retrieve()
                .bodyToFlux(Currency.class);
    }
}
