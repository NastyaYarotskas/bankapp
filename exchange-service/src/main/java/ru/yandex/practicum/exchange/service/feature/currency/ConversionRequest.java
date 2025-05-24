package ru.yandex.practicum.exchange.service.feature.currency;

import lombok.Data;

@Data
public class ConversionRequest {
    private String fromCurrency;
    private String toCurrency;
    private int amount;
}
