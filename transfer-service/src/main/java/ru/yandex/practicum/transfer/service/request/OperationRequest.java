package ru.yandex.practicum.transfer.service.request;

public record OperationRequest(String userId, String operationType, double amount) {
}