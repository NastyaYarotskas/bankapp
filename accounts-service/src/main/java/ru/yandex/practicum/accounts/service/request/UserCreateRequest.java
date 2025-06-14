package ru.yandex.practicum.accounts.service.request;

import lombok.*;

@Data
@Builder
public class UserCreateRequest {
    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
