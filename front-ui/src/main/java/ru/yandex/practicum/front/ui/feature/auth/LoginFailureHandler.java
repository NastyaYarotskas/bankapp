package ru.yandex.practicum.front.ui.feature.auth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginFailureHandler implements ServerAuthenticationFailureHandler {
    private final MeterRegistry meterRegistry;

    public LoginFailureHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        return exchange.getFormData().doOnNext(data -> {
            String username = data.getFirst("username");
            if (username != null) {
                Counter.builder("login_failure_total")
                        .description("Failed user logins")
                        .tag("login", username)
                        .register(meterRegistry)
                        .increment();
            }
        }).then(webFilterExchange.getChain().filter(exchange));
    }
} 