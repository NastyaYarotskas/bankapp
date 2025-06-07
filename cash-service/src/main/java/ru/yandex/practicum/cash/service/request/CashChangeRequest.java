package ru.yandex.practicum.cash.service.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CashChangeRequest {
    private String currency;
    private int value;
    private Action action;

    public enum Action {
        PUT, GET
    }
}
