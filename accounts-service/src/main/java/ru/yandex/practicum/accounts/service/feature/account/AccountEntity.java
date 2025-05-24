package ru.yandex.practicum.accounts.service.feature.account;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("accounts")
public class AccountEntity {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    private String currency;
    private int value;
    private boolean exists;
}
