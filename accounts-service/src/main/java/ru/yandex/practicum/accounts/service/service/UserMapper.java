package ru.yandex.practicum.accounts.service.service;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.accounts.service.entity.UserEntity;
import ru.yandex.practicum.accounts.service.model.Account;
import ru.yandex.practicum.accounts.service.entity.AccountEntity;
import ru.yandex.practicum.accounts.service.model.Currency;
import ru.yandex.practicum.accounts.service.model.CurrencyEnum;
import ru.yandex.practicum.accounts.service.model.User;

import java.util.List;

@UtilityClass
public class UserMapper {

    public static User toUser(UserEntity userEntity, List<AccountEntity> accountEntities) {
        List<Account> accounts = accountEntities.stream()
                .map(UserMapper::mapToAccount)
                .toList();

        return User.builder()
                .id(userEntity.getId())
                .login(userEntity.getLogin())
                .name(userEntity.getName())
                .password(userEntity.getPassword())
                .birthdate(userEntity.getBirthdate())
                .accounts(accounts)
                .build();
    }

    private static Account mapToAccount(AccountEntity accountEntity) {
        CurrencyEnum currencyEnum = CurrencyEnum.valueOf(accountEntity.getCurrency());
        Account account = Account.builder()
                .currency(new Currency(currencyEnum.getTitle(), currencyEnum.name()))
                .value(accountEntity.getValue())
                .exists(accountEntity.isExists())
                .build();
        account.setId(accountEntity.getId());
        return account;
    }
}
