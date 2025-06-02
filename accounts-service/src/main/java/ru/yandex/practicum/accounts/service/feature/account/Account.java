package ru.yandex.practicum.accounts.service.feature.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.accounts.service.feature.currency.Currency;

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
