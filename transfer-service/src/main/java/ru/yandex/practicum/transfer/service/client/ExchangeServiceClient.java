package ru.yandex.practicum.transfer.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.transfer.service.model.Currency;

@Component
public class ExchangeServiceClient extends BaseClient {

    @Autowired
    @Qualifier("exchangeServiceWebClient")
    private WebClient exchangeServiceWebClient;

    public Flux<Currency> getCurrencyRates() {
        return retrieveToken()
                .flatMapMany(
                        accessToken -> exchangeServiceWebClient.get()
                                .uri("/api/currencies")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToFlux(Currency.class)
                );
    }
}
