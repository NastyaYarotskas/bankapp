package ru.yandex.practicum.exchange.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) throws Exception {
        return security
                .authorizeExchange(authorizeRequests ->
                        authorizeRequests
                                .pathMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/index.html",
                                        "/api/currencies/*"
                                ).permitAll()
//                                .pathMatchers("/api/currencies/*").hasAuthority("SCOPE_resource.read") //Require scope
                                .anyExchange().permitAll() // All other requests need authentication
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .jwt(jwtSpec -> {
                            ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
                                List<String> roles = jwt.getClaim("roles") == null ? List.of() : jwt.getClaim("roles");

                                return Flux.fromIterable(roles)
                                        .map(SimpleGrantedAuthority::new);
                            });

                            jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter);
                        })
                )
                .build();
    }
}
