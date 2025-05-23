package ru.yandex.practicum.accounts.service.feature.user;

import lombok.Data;

@Data
public class UserInfoResponse {
    private String login;
    private String name;
}
