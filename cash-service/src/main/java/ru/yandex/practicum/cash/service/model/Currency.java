package ru.yandex.practicum.cash.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Currency {
    private String title;
    private String name;
}
