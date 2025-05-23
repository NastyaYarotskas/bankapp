package ru.yandex.practicum.front.ui.feature.account;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Account {
    private UUID id;
    private Currency currency;
    private int value;
    private boolean exists;
}
