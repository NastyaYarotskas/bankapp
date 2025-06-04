package ru.yandex.practicum.accounts.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class OAuth2ClientConfig {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) {
        return security
                .authorizeExchange(requests -> requests
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/index.html",
                                "/actuator/health"
                        ).permitAll()
                        .pathMatchers("/api/v1/users/**").hasAnyAuthority("SCOPE_accounts.write", "SCOPE_accounts.read")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .jwt(jwtSpec -> {
                            ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
                            converter.setJwtGrantedAuthoritiesConverter(
                                    new ReactiveJwtGrantedAuthoritiesConverterAdapter(
                                            new JwtGrantedAuthoritiesConverter()
                                    )
                            );
                            jwtSpec.jwtAuthenticationConverter(converter);
                        })
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
