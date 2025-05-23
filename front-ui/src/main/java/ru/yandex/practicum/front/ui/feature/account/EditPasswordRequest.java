package ru.yandex.practicum.front.ui.feature.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EditPasswordRequest {
    private String login;
    private String password;
    private String confirmPassword;
}
