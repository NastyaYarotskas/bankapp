package ru.yandex.practicum.accounts.service.service;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.service.model.User;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static ru.yandex.practicum.accounts.service.message.UserValidationErrorMessages.*;

@UtilityClass
public class UserValidator {

    public static Mono<ResponseStatusException> validatePasswordChange(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, CONFIRMATION_ERROR_MSG));
        } else {
            return Mono.empty();
        }
    }

    public static Mono<ResponseStatusException> validateBirthdate(String birthdate) {
        if (birthdate == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, EMPTY_BIRTHDAY_ERROR_MSG));
        }
        
        try {
            LocalDate date = LocalDate.parse(birthdate);
            OffsetDateTime birthdateOffset = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime eighteenYearsAgo = OffsetDateTime.now(ZoneOffset.UTC).minusYears(18);
        
            if (birthdateOffset.isAfter(eighteenYearsAgo)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_ERROR_MSG));
            }
            return Mono.empty();
        } catch (DateTimeParseException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_FORMAT_ERROR_MSG));
        }
    }

    public static Mono<ResponseStatusException> validateBirthdate(OffsetDateTime birthdate) {
        if (birthdate == null) {
            return Mono.empty();
        }

        try {
            OffsetDateTime eighteenYearsAgo = OffsetDateTime.now(ZoneOffset.UTC).minusYears(18);

            if (birthdate.isAfter(eighteenYearsAgo)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_ERROR_MSG));
            }
            return Mono.empty();
        } catch (DateTimeParseException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_BIRTHDAY_FORMAT_ERROR_MSG));
        }
    }

    public static Mono<Void> validateAccounts(User user) {
        boolean hasInvalidAccounts = user.getAccounts().stream()
                .anyMatch(account -> !account.isExists() && account.getValue() != 0);

        if (hasInvalidAccounts) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Для отключенных аккаунтов (exists=false) значение value должно быть 0"));
        }
        return Mono.empty();
    }
}