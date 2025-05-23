package ru.yandex.practicum.front.ui.feature.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AccountDetails extends User {
    private UUID id;
    private OffsetDateTime birthdate;
    private List<Account> accounts;

    @Builder(builderMethodName = "accountDetailsBuilder")
    public AccountDetails(String username,
                          String password,
                          boolean enabled,
                          boolean accountNonExpired,
                          boolean credentialsNonExpired,
                          boolean accountNonLocked,
                          Collection<? extends GrantedAuthority> authorities,
                          UUID id,
                          OffsetDateTime birthdate,
                          List<Account> accounts) {
        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.birthdate = birthdate;
        this.accounts = accounts;
    }
}
