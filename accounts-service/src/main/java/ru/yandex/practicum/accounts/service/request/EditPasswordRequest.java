package ru.yandex.practicum.accounts.service.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditPasswordRequest {
    private String password;
    private String confirmPassword;
}
