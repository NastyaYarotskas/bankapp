package ru.yandex.practicum.accounts.service.feature.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditPasswordRequest {
    private String password;
    private String confirmPassword;
}
