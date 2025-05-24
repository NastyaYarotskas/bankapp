package ru.yandex.practicum.exchange.service.feature.currency;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final List<Currency> currencies = new ArrayList<>();

    public CurrencyController() {
        currencies.add(new Currency("USD", "Доллары", 1));
        currencies.add(new Currency("CNY", "Юани", 1));
        currencies.add(new Currency("RUB", "Рубли", 1));
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

    @PostMapping("/convert")
    public Mono<ConversionResponse> convert(@RequestBody ConversionRequest request) {
        return Mono.just(request)
                .flatMap(this::calculateConversionRate)
                .map(tuple -> buildResponse(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Tuple2<ConversionRequest, Double>> calculateConversionRate(ConversionRequest request) {
        String fromCurrency = request.getFromCurrency();
        String toCurrency = request.getToCurrency();

        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return Mono.just(Tuples.of(request, 1.0));
        }

        if (fromCurrency.equalsIgnoreCase("RUB")) {
            return getCurrency(toCurrency)
                    .map(currency -> Tuples.of(request, currency.getValue()));
        } else if (toCurrency.equalsIgnoreCase("RUB")) {
            return getCurrency(fromCurrency)
                    .map(currency -> Tuples.of(request, 1 / currency.getValue()));
        } else {
            return Mono.zip(getCurrency(fromCurrency), getCurrency(toCurrency))
                    .map(rates -> {
                        double fromRate = rates.getT1().getValue();
                        double toRate = rates.getT2().getValue();
                        return Tuples.of(request, toRate / fromRate);
                    });
        }
    }

    private ConversionResponse buildResponse(ConversionRequest request, double rate) {
        return ConversionResponse.builder()
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .originalAmount(request.getAmount())
                .convertedAmount((int) (request.getAmount() * rate))
                .rate(rate)
                .build();
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
}