package ru.yandex.practicum.accounts.service.feature.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
