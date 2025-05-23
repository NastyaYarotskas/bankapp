package ru.yandex.practicum.front.ui.feature.account;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CreateUserRequest {
    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
