package ru.yandex.practicum.transfer.service.feature.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.transfer.service.feature.transfer.model.Currency;

@Component
public class ExchangeServiceClient {

    private final WebClient exchangeServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public ExchangeServiceClient(@LoadBalanced WebClient.Builder exchangeServiceWebClientBuilder) {
        this.exchangeServiceWebClient = exchangeServiceWebClientBuilder.build();
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

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("transfer-service-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
