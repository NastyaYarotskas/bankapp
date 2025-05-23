package ru.yandex.practicum.accounts.service.feature.user;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.accounts.service.feature.account.Account;

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
