package ru.yandex.practicum.front.ui.feature.cash;

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
