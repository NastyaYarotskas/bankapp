package ru.yandex.practicum.exchange.generator.service.feature.currency;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Currency {
    private String name;
    private String title;
    private double value;
}
