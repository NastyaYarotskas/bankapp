package ru.yandex.practicum.transfer.service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.transfer.service.model.Currency;

@Component
public class ExchangeServiceClient extends BaseClient {

    private final WebClient exchangeServiceWebClient;

    public ExchangeServiceClient(WebClient.Builder webClientBuilder, @Value("${exchange.service.url}") String baseUrl) {
        this.exchangeServiceWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

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
