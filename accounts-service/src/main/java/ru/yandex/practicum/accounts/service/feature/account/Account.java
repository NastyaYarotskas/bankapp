package ru.yandex.practicum.accounts.service.feature.account;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.accounts.service.feature.currency.Currency;

import java.util.UUID;

@Data
@Builder
public class Account {
    private UUID id;
    private Currency currency;
    private int value;
    private boolean exists;
}
