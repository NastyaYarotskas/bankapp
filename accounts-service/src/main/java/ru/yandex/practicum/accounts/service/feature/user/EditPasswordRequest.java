package ru.yandex.practicum.accounts.service.feature.user;

import lombok.Data;

@Data
public class EditPasswordRequest {
    private String password;
    private String confirmPassword;
}
