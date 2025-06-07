package ru.yandex.practicum.transfer.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Currency {
    private String title;
    private String name;
    private double value;
}
