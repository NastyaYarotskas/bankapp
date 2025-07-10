package ru.yandex.practicum.exchange.service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final List<Currency> currencies = new ArrayList<>();

    public CurrencyController() {
        currencies.add(new Currency("USD", "Dollars", 1));
        currencies.add(new Currency("CNY", "Yuan", 1));
        currencies.add(new Currency("RUB", "Rubles", 1));
    }

    @GetMapping
    public Flux<Currency> getAllCurrencies() {
        return Flux.fromIterable(currencies);
    }

    @GetMapping("/{name}")
    public Mono<Currency> getCurrency(@PathVariable String name) {
        return Mono.justOrEmpty(currencies.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(name)).findAny())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Валюта не найдена")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Currency> addCurrency(@RequestBody Currency currency) {
        return Mono.just(currency)
                .doOnNext(currencies::add);
    }

    @PutMapping("/{code}")
    public Mono<Currency> updateCurrency(@PathVariable String code, @RequestBody Currency updatedCurrency) {
        return getCurrency(code)
                .doOnNext(currency -> currency.setValue(updatedCurrency.getValue()))
                .thenReturn(updatedCurrency);
    }

    @KafkaListener(topics = "currency-rates", groupId = "exchange-service")
    public void listen(@Payload Currency currency) {
        Optional<Currency> any = currencies.stream()
                .filter(c -> c.getName().equalsIgnoreCase(currency.getName()))
                .findAny();
        any.ifPresent(currency1 -> currency1.setValue(currency.getValue()));
    }
}