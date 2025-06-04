package ru.yandex.practicum.notification.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/notifications").hasAuthority("SCOPE_notification.write") //Require scope
                                .anyRequest().permitAll() // All other requests need authentication
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {}) // Enable JWT-based authentication for the resource server
                );
        return http.build();
    }
}
