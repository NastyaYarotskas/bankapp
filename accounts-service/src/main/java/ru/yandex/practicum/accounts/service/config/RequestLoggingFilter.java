package ru.yandex.practicum.accounts.service.config;

import io.micrometer.tracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(0) // Ставим пораньше
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        return chain.filter(exchange)
                .doOnEach(signal -> {
                    if (!signal.isOnComplete() && !signal.isOnNext()) return;

                    Span span = signal.getContextView().getOrDefault(Span.class, null);
                    String traceId = (span != null) ? span.context().traceId() : MDC.get("traceId");
                    String spanId = (span != null) ? span.context().spanId() : MDC.get("spanId");

                    String method = request.getMethod().name();
                    String path = request.getURI().getPath();

                    log.info("📥 Incoming HTTP {} {} | traceId={} | spanId={}", method, path, traceId, spanId);

                    request.getHeaders().forEach((key, values) -> {
                        values.forEach(value -> {
                            log.debug("🔸 Header: {} = {}", key, value);
                        });
                    });
                });
    }
}
