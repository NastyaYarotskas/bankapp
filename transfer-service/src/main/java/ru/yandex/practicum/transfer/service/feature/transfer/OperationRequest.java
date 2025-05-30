package ru.yandex.practicum.transfer.service.feature.transfer;

public record OperationRequest(String userId, String operationType, double amount) {
}