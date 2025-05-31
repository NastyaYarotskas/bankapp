package ru.yandex.practicum.front.ui.feature.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.front.ui.feature.account.model.Currency;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class CurrencyControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    @Test
    void getCurrencyRates_ShouldReturnCurrencies() {
        // Подготовка тестовых данных
        Currency usd = new Currency("USD", "US Dollar", 1.0);
        Currency eur = new Currency("EUR", "Euro", 1.1);
        List<Currency> currencies = Arrays.asList(usd, eur);

        // Настройка поведения мока
        when(exchangeServiceClient.getCurrencyRates())
                .thenReturn(Flux.fromIterable(currencies));

        // Выполнение запроса и проверка результата
        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Currency.class)
                .hasSize(2)
                .contains(usd, eur);
    }

    @Test
    void getCurrencyRates_WhenEmpty_ShouldReturnEmptyList() {
        // Настройка поведения мока для пустого результата
        when(exchangeServiceClient.getCurrencyRates())
                .thenReturn(Flux.empty());

        // Выполнение запроса и проверка результата
        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Currency.class)
                .hasSize(0);
    }

    @Test
    void getCurrencyRates_WhenError_ShouldReturn5xx() {
        // Настройка поведения мока для ошибки
        when(exchangeServiceClient.getCurrencyRates())
                .thenReturn(Flux.error(new RuntimeException("Service unavailable")));

        // Выполнение запроса и проверка результата
        webTestClient.get()
                .uri("/api/rates")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}