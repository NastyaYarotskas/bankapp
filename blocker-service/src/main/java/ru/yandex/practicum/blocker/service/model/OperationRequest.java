package ru.yandex.practicum.blocker.service.model;

public record OperationRequest(String userId, String operationType, double amount) {
}