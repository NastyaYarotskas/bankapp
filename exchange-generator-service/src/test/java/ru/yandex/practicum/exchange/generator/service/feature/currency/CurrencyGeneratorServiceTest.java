package ru.yandex.practicum.exchange.generator.service.feature.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CurrencyGeneratorServiceTest {

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    private CurrencyGeneratorService currencyGeneratorService;

    @Captor
    private ArgumentCaptor<Mono<Currency>> currencyCaptor;

    @BeforeEach
    void setUp() {
        when(exchangeServiceClient.updateCurrencyRate(any(), any())).thenReturn(Mono.empty());
        currencyGeneratorService = new CurrencyGeneratorService(exchangeServiceClient);
    }

    @Test
    void generateAndUpdateCurrencyRates_ShouldUpdateAllCurrencies() {
        currencyGeneratorService.generateAndUpdateCurrencyRates();

        verify(exchangeServiceClient, times(4))
                .updateCurrencyRate(any(), any());
    }

    @Test
    void generateAndUpdateCurrencyRates_ShouldUpdateUSDCurrency() {
        currencyGeneratorService.generateAndUpdateCurrencyRates();

        verify(exchangeServiceClient)
                .updateCurrencyRate(eq("USD"), currencyCaptor.capture());

        Currency usdCurrency = currencyCaptor.getValue().block();
        assertNotNull(usdCurrency);
        assertEquals("USD", usdCurrency.getName());
        assertEquals("Доллары", usdCurrency.getTitle());
        assertTrue(usdCurrency.getValue() >= 1.0 && usdCurrency.getValue() <= 3.0);
    }

    @Test
    void generateAndUpdateCurrencyRates_ShouldGenerateValidRates() {
        currencyGeneratorService.generateAndUpdateCurrencyRates();

        verify(exchangeServiceClient, times(3))
                .updateCurrencyRate(any(), currencyCaptor.capture());

        currencyCaptor.getAllValues().forEach(currencyMono -> {
            Currency currency = currencyMono.block();
            assertNotNull(currency);
            double rate = currency.getValue();
            assertTrue(rate >= 1.0 && rate <= 3.0,
                    "Rate should be between 1.0 and 3.0, but was: " + rate);

            String rateStr = String.valueOf(rate);
            int decimalPlaces = rateStr.length() - rateStr.indexOf('.') - 1;
            assertTrue(decimalPlaces <= 2,
                    "Rate should have at most 2 decimal places, but was: " + rate);
        });
    }
}
