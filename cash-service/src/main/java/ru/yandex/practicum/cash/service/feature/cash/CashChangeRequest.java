package ru.yandex.practicum.cash.service.feature.cash;

import lombok.Data;

@Data
public class CashChangeRequest {
    private String currency;
    private int value;
    private Action action;

    public enum Action {
        PUT, GET
    }
}
