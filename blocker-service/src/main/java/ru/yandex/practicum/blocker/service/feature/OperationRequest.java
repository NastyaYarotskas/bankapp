package ru.yandex.practicum.blocker.service.feature;

public record OperationRequest(String userId, String operationType, double amount) {
}