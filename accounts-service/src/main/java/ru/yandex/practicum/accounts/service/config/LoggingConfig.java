package ru.yandex.practicum.accounts.service.config;

import io.micrometer.tracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingConfig implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(ctx -> {
                    Span span = ctx.getOrDefault(Span.class, null);
                    if (span != null) {
                        MDC.put("traceId", span.context().traceId());
                        MDC.put("spanId", span.context().spanId());
                    }
                    return ctx;
                })
                .doFinally(signalType -> {
                    // важно очищать MDC после завершения запроса!
                    MDC.clear();
                });
    }
}