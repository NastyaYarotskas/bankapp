package ru.yandex.practicum.accounts.service.feature.notification;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NotificationMessages {
    public static final String SUCCESS_PASSWORD_UPDATE_MESSAGE = "Пароль успешно обновлен";
    public static final String ERROR_PASSWORD_UPDATE_MESSAGE_TEMPLATE = "Не удалось обновить пароль. Причина: %s";
    public static final String ACCOUNT_UPDATE_MESSAGE_TEMPLATE =
            "Данные вашего аккаунта успешно обновлены. Изменено счетов: %d";
}
