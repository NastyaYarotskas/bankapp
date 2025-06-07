package ru.yandex.practicum.transfer.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID id;
    private String login;
    private String name;
    private String password;
    private String birthdate;
    private List<Account> accounts;
}
