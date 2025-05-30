package ru.yandex.practicum.cash.service.feature.cash;

public record OperationCheckResult(boolean blocked, String message, String detectionAlgorithm) {
    public static OperationCheckResult ok() {
        return new OperationCheckResult(false, "Operation allowed", null);
    }
}
