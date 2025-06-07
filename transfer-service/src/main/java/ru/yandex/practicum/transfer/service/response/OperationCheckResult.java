package ru.yandex.practicum.transfer.service.response;

public record OperationCheckResult(boolean blocked, String message, String detectionAlgorithm) {
}
