package ru.yandex.practicum.exchange.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Currency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();

    public CurrencyController() {
        currencies.put("USD", new Currency("USD", "Dollars", 1));
        currencies.put("CNY", new Currency("CNY", "Yuan", 1));
        currencies.put("RUB", new Currency("RUB", "Rubles", 1));
    }

    @GetMapping
    public Flux<Currency> getAllCurrencies() {
        return Flux.fromIterable(currencies.values());
    }

    @GetMapping("/{name}")
    public Mono<Currency> getCurrency(@PathVariable String name) {
        return Mono.justOrEmpty(currencies.get(name))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Валюта не найдена")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Currency> addCurrency(@RequestBody Currency currency) {
        return Mono.just(currency)
                .doOnNext(newCurrency -> currencies.put(currency.getName(), currency));
    }

    @PutMapping("/{code}")
    public Mono<Currency> updateCurrency(@PathVariable String code, @RequestBody Currency updatedCurrency) {
        return getCurrency(code)
                .doOnNext(currency -> currency.setValue(updatedCurrency.getValue()))
                .thenReturn(updatedCurrency);
    }

    @KafkaListener(topics = "currency-rates", groupId = "exchange-service")
    public void listen(@Payload Currency currency) {
        log.info("Got currency: {}", currency);
        currencies.put(currency.getName(), currency);
    }
}