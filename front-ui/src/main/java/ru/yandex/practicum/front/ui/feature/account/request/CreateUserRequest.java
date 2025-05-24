package ru.yandex.practicum.front.ui.feature.account.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
