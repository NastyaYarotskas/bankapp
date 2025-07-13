package ru.yandex.practicum.exchange.generator.service.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Currency;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class CurrencyGeneratorService {
    private final CurrencyRateProducer currencyRateProducer;
    private final Random random;
    private final List<String> currencyCodes;

    public CurrencyGeneratorService(CurrencyRateProducer currencyRateProducer) {
        this.currencyRateProducer = currencyRateProducer;
        this.random = new Random();
        this.currencyCodes = List.of("USD", "CNY", "RUB");
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void generateAndUpdateCurrencyRates() {
        currencyCodes.forEach(code -> {
            double newRate = 1.0 + random.nextDouble() * 2.0;
            double roundedRate = Math.round(newRate * 100.0) / 100.0;
            Currency updatedCurrency = new Currency(code, getTitleForCode(code), roundedRate);
            currencyRateProducer.sendCurrencyRate(updatedCurrency);
        });
    }

    private String getTitleForCode(String code) {
        return switch (code) {
            case "USD" -> "Dollars";
            case "CNY" -> "Yuan";
            case "RUB" -> "Rubles";
            default -> code;
        };
    }
}

