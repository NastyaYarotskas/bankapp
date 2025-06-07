package ru.yandex.practicum.cash.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private UUID id;
    private Currency currency;
    private int value;
    private boolean exists;
}
