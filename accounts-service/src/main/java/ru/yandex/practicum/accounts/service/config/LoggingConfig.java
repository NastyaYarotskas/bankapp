package ru.yandex.practicum.accounts.service.config;

import brave.Tracer;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class LoggingConfig implements WebFilter {

    private final Tracer tracer;

    public LoggingConfig(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(context -> {
                    // Получаем текущий span
                    brave.Span currentSpan = tracer.currentSpan();
                    if (currentSpan != null) {
                        // Добавляем trace id и span id в MDC
                        MDC.put("traceId", currentSpan.context().traceIdString());
                        MDC.put("spanId", currentSpan.context().spanIdString());
                    }
                    return context;
                })
                .doFinally(signalType -> {
                    // Очищаем MDC после завершения запроса
                    MDC.clear();
                });
    }
} 