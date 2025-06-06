package ru.yandex.practicum.accounts.service.feature;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration
public class OAuth2ClientTestConfig {

    @Bean
    @Primary
    SecurityWebFilterChain testSecurityFilterChain(ServerHttpSecurity security) {
        return security
                .authorizeExchange(requests -> requests
                        .anyExchange().permitAll()
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
