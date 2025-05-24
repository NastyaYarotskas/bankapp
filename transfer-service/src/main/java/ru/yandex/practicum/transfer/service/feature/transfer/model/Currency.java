package ru.yandex.practicum.transfer.service.feature.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Currency {
    private String title;
    private String name;
    private double value;
}
