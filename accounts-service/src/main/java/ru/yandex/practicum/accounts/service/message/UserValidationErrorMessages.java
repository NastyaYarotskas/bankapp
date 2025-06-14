package ru.yandex.practicum.accounts.service.message;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserValidationErrorMessages {

    public static final String CONFIRMATION_ERROR_MSG = "Пароль и подтверждение пароля не совпадают";
    public static final String EMPTY_BIRTHDAY_ERROR_MSG = "Дата рождения должна быть заполнена";
    public static final String INVALID_BIRTHDAY_ERROR_MSG = "Пользователь должен быть старше 18 лет";
    public static final String INVALID_BIRTHDAY_FORMAT_ERROR_MSG = "Неверный формат даты";
    public static final String EMPTY_REQUEST_ERROR_MSG = "Запрос не может быть пустым";
    public static final String LOGIN_ERROR_MSG = "Пользователь с логином %s уже существует";
    public static final String USER_NOT_FOUND_ERROR_MSG = "Пользователь с логином %s не найден";
    public static final String INVALID_ACCOUNT_LIST_ERROR_MSG = "Некорректный список счетов";
    public static final String ACCOUNT_WITH_ID_NOT_SOUND_FORMAT_ERROR_MSG = "Аккаунт с ID %s не найден";
}
