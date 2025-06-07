package ru.yandex.practicum.transfer.service.request;

import lombok.Data;

@Data
public class TransferRequest {
    private String login;
    private String fromCurrency;
    private String toCurrency;
    private int value;
    private String toLogin;
}
