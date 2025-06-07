package ru.yandex.practicum.exchange.generator.service.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.exchange.generator.service.config.TestWebClientConfig;
import ru.yandex.practicum.exchange.generator.service.config.TestOAuth2ClientConfig;

@SpringBootTest
@AutoConfigureStubRunner(
        ids = "ru.yandex.practicum:exchange-service:+:stubs:9001",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({TestWebClientConfig.class, TestOAuth2ClientConfig.class})
public class CurrencyGeneratorServiceTest {

    @Autowired
    private CurrencyGeneratorService currencyGeneratorService;

    @Test
    void generateAndUpdateCurrencyRates_sunnyDay_shouldUpdateAllCurrencies() {
        currencyGeneratorService.generateAndUpdateCurrencyRates();
    }
}
