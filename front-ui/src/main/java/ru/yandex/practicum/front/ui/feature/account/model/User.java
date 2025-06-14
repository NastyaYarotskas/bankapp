package ru.yandex.practicum.front.ui.feature.account.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class User {
    private UUID id;
    private String login;
    private String name;
    private String password;
    private OffsetDateTime birthdate;
    private List<Account> accounts;
}
