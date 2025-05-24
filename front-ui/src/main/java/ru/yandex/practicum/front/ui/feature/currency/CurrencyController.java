package ru.yandex.practicum.front.ui.feature.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.front.ui.feature.account.model.Currency;

@RestController
public class CurrencyController {

    @Autowired
    private ExchangeServiceClient exchangeServiceClient;

    @GetMapping(value = "/api/rates")
    public Flux<Currency> getCurrencyRates() {
        return exchangeServiceClient.getCurrencyRates();
    }
}
