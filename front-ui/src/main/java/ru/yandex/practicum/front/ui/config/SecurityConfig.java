package ru.yandex.practicum.front.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.front.ui.feature.auth.LoginSuccessHandler;
import ru.yandex.practicum.front.ui.feature.auth.LoginFailureHandler;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/main", "/signup", "/login", "/api/rates", "/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .formLogin(form -> form
                        .authenticationSuccessHandler(loginSuccessHandler)
                        .authenticationFailureHandler(loginFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(createCompositeLogoutHandler()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    private ServerLogoutHandler createCompositeLogoutHandler() {
        return new DelegatingServerLogoutHandler(
                new WebSessionServerLogoutHandler(),
                new SecurityContextServerLogoutHandler(),
                (exchange, authentication) -> {
                    exchange.getExchange().getResponse()
                            .addCookie(ResponseCookie.from("JSESSIONID", "")
                                    .maxAge(0)
                                    .path("/")
                                    .build());
                    return Mono.empty();
                });
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
