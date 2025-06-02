package ru.yandex.practicum.exchange.generator.service.feature.currency;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class CurrencyGeneratorService {
    private final ExchangeServiceClient exchangeServiceClient;
    private final Random random;
    private final List<String> currencyCodes;

    public CurrencyGeneratorService(ExchangeServiceClient exchangeServiceClient) {
        this.exchangeServiceClient = exchangeServiceClient;
        this.random = new Random();
        this.currencyCodes = List.of("USD", "CNY", "RUB");
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void generateAndUpdateCurrencyRates() {
        currencyCodes.forEach(code -> {
            double newRate = 1.0 + random.nextDouble() * 2.0;
            double roundedRate = Math.round(newRate * 100.0) / 100.0;
            Currency updatedCurrency = new Currency(code, getTitleForCode(code), roundedRate);
            exchangeServiceClient.updateCurrencyRate(code, Mono.just(updatedCurrency))
                    .subscribe();
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

