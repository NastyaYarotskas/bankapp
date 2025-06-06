package ru.yandex.practicum.exchange.generator.service.feature.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ExchangeServiceClient {

    @Autowired
    private WebClient exchangeServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public Mono<Currency> updateCurrencyRate(String code, Mono<Currency> updatedCurrency) {
        return retrieveToken()
                .flatMap(accessToken ->
                    exchangeServiceWebClient.put()
                            .uri("/api/currencies/" + code)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .body(updatedCurrency, Currency.class)
                            .retrieve()
                            .bodyToMono(Currency.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("exchange-generator-service-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
