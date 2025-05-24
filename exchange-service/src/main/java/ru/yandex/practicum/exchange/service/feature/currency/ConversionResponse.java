package ru.yandex.practicum.exchange.service.feature.currency;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversionResponse {
    private String fromCurrency;
    private String toCurrency;
    private int originalAmount;
    private int convertedAmount;
    private double rate;
}
