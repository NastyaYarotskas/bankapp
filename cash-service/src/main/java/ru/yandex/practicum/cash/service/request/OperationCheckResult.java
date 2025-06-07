package ru.yandex.practicum.cash.service.request;

public record OperationCheckResult(boolean blocked, String message, String detectionAlgorithm) {
}
