package ru.yandex.practicum.front.ui.feature.auth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import java.net.URI;

@Component
public class LoginSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final MeterRegistry meterRegistry;

    public LoginSuccessHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        String username = authentication.getName();
        Counter.builder("login_success_total")
                .description("Successful user logins")
                .tag("login", username)
                .register(meterRegistry)
                .increment();
        // Редирект на главную страницу
        ServerWebExchange exchange = webFilterExchange.getExchange();
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create("/main"));
        return exchange.getResponse().setComplete();
    }
} 