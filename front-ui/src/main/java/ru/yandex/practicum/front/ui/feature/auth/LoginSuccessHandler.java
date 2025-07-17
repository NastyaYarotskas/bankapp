package ru.yandex.practicum.front.ui.feature.auth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
        return webFilterExchange.getChain().filter(webFilterExchange.getExchange());
    }
} 