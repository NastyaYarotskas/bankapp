package ru.yandex.practicum.front.ui.feature.account.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String login;
    private String name;
    private String password;
    private String confirmPassword;
    private String birthdate;
}
