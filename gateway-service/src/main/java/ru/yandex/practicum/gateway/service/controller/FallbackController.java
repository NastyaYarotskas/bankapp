package ru.yandex.practicum.gateway.service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/exchange-service-fallback")
    public Mono<ResponseEntity<Map<String, String>>> exchangeServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис обмена валют временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/accounts-service-fallback")
    public Mono<ResponseEntity<Map<String, String>>> accountsServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис счетов временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/cash-service-fallback")
    public Mono<ResponseEntity<Map<String, String>>> cashServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис наличных операций временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/transfer-service-fallback")
    public Mono<ResponseEntity<Map<String, String>>> transferServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис переводов временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }

    @GetMapping("/blocker-service-fallback")
    public Mono<ResponseEntity<Map<String, String>>> blockerServiceFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Сервис блокировок временно недоступен");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }
}
