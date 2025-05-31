package ru.yandex.practicum.front.ui.feature.account.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditPasswordRequest {
    private String login;
    private String password;
    private String confirmPassword;
}
