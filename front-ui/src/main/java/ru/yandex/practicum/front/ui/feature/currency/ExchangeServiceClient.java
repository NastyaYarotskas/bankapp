package ru.yandex.practicum.front.ui.feature.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.account.model.Currency;

@Component
public class ExchangeServiceClient {

    private final WebClient exchangeServiceWebClient;
    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    public ExchangeServiceClient(WebClient.Builder exchangeServiceWebClientBuilder) {
        this.exchangeServiceWebClient = exchangeServiceWebClientBuilder.build();
    }

    public Flux<Currency> getCurrencyRates() {
        return retrieveToken()
                .flatMapMany(accessToken ->
                        exchangeServiceWebClient.get()
                                .uri("/api/currencies")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .retrieve()
                                .bodyToFlux(Currency.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("front-ui-client")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
