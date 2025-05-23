package ru.yandex.practicum.front.ui.feature.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

@Service
public class AccountDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountClient accountClient;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return accountClient.getAccountDetails(username)
                .map(account ->
                        AccountDetails.accountDetailsBuilder()
                                .id(account.getId())
                                .username(account.getLogin())
                                .password(passwordEncoder.encode(account.getPassword()))
                                .birthdate(account.getBirthdate())
                                .accounts(account.getAccounts())
                                .authorities(Arrays.stream("USER".split(","))
                                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList())
                                .accountNonExpired(true)
                                .credentialsNonExpired(true)
                                .accountNonLocked(true)
                                .enabled(true)
                                .build()
                );
    }
}
