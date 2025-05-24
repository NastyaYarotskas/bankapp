package ru.yandex.practicum.front.ui.feature.transfer;

import lombok.Data;

@Data
public class TransferRequest {
    private String login;
    private String fromCurrency;
    private String toCurrency;
    private int value;
    private String toLogin;
}
