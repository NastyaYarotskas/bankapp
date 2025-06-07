package ru.yandex.practicum.accounts.service.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@Table("users")
public class UserEntity {
    @Id
    private UUID id;
    private String login;
    private String name;
    private String password;
    private OffsetDateTime birthdate;
}
