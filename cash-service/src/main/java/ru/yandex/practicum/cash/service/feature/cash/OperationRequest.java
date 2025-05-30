package ru.yandex.practicum.cash.service.feature.cash;

public record OperationRequest(String userId, String operationType, double amount) {
}