package ru.yandex.practicum.cash.service.request;

public record OperationRequest(String userId, String operationType, double amount) {
}