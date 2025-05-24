package ru.yandex.practicum.front.ui.feature.account;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import ru.yandex.practicum.front.ui.feature.account.model.User;
import ru.yandex.practicum.front.ui.feature.account.request.CreateUserRequest;
import ru.yandex.practicum.front.ui.feature.error.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class UserModelAttributes {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void populateUserAttributes(Model model, User user) {
        model.addAttribute("login", user.getLogin());
        model.addAttribute("name", user.getName());
        model.addAttribute("birthdate", formatBirthdate(user.getBirthdate()));
        model.addAttribute("accounts", user.getAccounts());
    }

    public void populateSignupModel(Model model, CreateUserRequest request, ErrorResponse error) {
        model.addAttribute("errors", List.of(error.error()));
        model.addAttribute("login", request.getLogin());
        model.addAttribute("name", request.getName());
        model.addAttribute("birthdate", request.getBirthdate());
    }

    private String formatBirthdate(OffsetDateTime birthdate) {
        return birthdate.format(DATE_FORMATTER);
    }
}

