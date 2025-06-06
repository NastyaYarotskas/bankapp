package ru.yandex.practicum.exchange.service.feature.currency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
public class CurrencyControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void getAllCurrencies_currenciesArePresent_shouldReturnAllCurrencies() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Currency.class)
                .hasSize(3)
                .contains(
                        new Currency("USD", "Dollars", 1),
                        new Currency("CNY", "Yuan", 1),
                        new Currency("RUB", "Rubles", 1)
                );
    }

    @Test
    void getCurrency_whenCurrencyExists_shouldReturnCurrency() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies/usd")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Currency.class)
                .isEqualTo(new Currency("USD", "Dollars", 1));
    }

    @Test
    void getCurrency_whenCurrencyExistsIgnoreCase_shouldReturnCurrency() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies/USD")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Currency.class)
                .isEqualTo(new Currency("USD", "Dollars", 1));
    }

    @Test
    void getCurrency_whenCurrencyNotExists_shouldReturnBadRequest() {
        webTestClient.mutateWith(getJwtMutator())
                .get()
                .uri("/api/currencies/EURO")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateCurrency_whenCurrencyExists_shouldUpdateAndReturnCurrency() {
        Currency updatedCurrency = new Currency("USD", "Dollars", 1.5);

        webTestClient.mutateWith(getJwtMutator())
                .put()
                .uri("/api/currencies/usd")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCurrency)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Currency.class)
                .isEqualTo(updatedCurrency);

        updatedCurrency = new Currency("USD", "Dollars", 1);

        webTestClient.mutateWith(getJwtMutator())
                .put()
                .uri("/api/currencies/usd")
                .bodyValue(updatedCurrency)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Currency.class)
                .isEqualTo(updatedCurrency);
    }

    @Test
    void updateCurrency_whenCurrencyNotExists_shouldReturnBadRequest() {
        Currency updatedCurrency = new Currency("EURO", "Euro", 0.85);

        webTestClient.mutateWith(getJwtMutator())
                .put()
                .uri("/api/currencies/EURO")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCurrency)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static SecurityMockServerConfigurers.JwtMutator getJwtMutator() {
        return mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_exchange.write"),
                new SimpleGrantedAuthority("SCOPE_exchange.read"));
    }
}
