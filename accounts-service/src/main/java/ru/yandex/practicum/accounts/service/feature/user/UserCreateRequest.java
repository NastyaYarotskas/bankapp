package ru.yandex.practicum.accounts.service.feature.user;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserCreateRequest {
    private UUID id;
    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
