package ru.yandex.practicum.transfer.service.feature.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.transfer.service.feature.transfer.model.Currency;

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
