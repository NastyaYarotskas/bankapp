package ru.yandex.practicum.exchange.generator.service.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.yandex.practicum.exchange.generator.service.config.TestOAuth2ClientConfig;

@SpringBootTest
@EmbeddedKafka(
        topics = {"currency-rates"}
)
@Import({TestOAuth2ClientConfig.class})
public class CurrencyGeneratorServiceTest {

    @Autowired
    private CurrencyGeneratorService currencyGeneratorService;

    @Test
    void generateAndUpdateCurrencyRates_sunnyDay_shouldUpdateAllCurrencies() {
        currencyGeneratorService.generateAndUpdateCurrencyRates();
    }
}
