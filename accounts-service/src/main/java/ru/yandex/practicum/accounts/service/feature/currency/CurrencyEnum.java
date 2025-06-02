package ru.yandex.practicum.accounts.service.feature.currency;

import lombok.Getter;

@Getter
public enum CurrencyEnum {

    RUB("Rubles"), USD("Dollars"), CNY("Yuan");

    private final String title;

    CurrencyEnum(String title) {
        this.title = title;
    }
}
