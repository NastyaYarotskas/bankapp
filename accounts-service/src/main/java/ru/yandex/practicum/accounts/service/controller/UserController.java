package ru.yandex.practicum.accounts.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.accounts.service.request.EditPasswordRequest;
import ru.yandex.practicum.accounts.service.request.UserCreateRequest;
import ru.yandex.practicum.accounts.service.entity.UserEntity;
import ru.yandex.practicum.accounts.service.service.UserService;
import ru.yandex.practicum.accounts.service.model.User;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Создание нового пользователя",
            description = "Создает нового пользователя на основе предоставленных данных"
    )
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    @PostMapping
    public Mono<User> createUser(
            @RequestBody @Valid
            @Parameter(description = "Данные для создания пользователя")
            UserCreateRequest request
    ) {
        return userService.createUser(request);
    }

    @Operation(
            summary = "Получение пользователя по логину",
            description = "Возвращает информацию о пользователе по его логину"
    )
    @ApiResponse(responseCode = "200", description = "Пользователь успешно найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/{login}")
    public Mono<User> getUser(
            @PathVariable
            @Parameter(description = "Логин пользователя")
            String login
    ) {
        return userService.getUserByLogin(login);
    }

    @Operation(
            summary = "Получение списка всех пользователей",
            description = "Возвращает список всех пользователей в системе с базовой информацией"
    )
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    @GetMapping
    public Flux<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(
            summary = "Изменение пароля пользователя",
            description = "Обновляет пароль пользователя по его логину"
    )
    @ApiResponse(responseCode = "200", description = "Пароль успешно обновлен")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @ApiResponse(responseCode = "400", description = "Некорректный пароль")
    @PostMapping("/{login}/editPassword")
    public Mono<User> editPassword(
            @PathVariable
            @Parameter(description = "Логин пользователя")
            String login,
            @RequestBody @Valid
            @Parameter(description = "Данные для изменения пароля")
            EditPasswordRequest editPasswordRequest
    ) {
        return userService.updateUserPassword(login, editPasswordRequest);
    }

    @Operation(
            summary = "Редактирование аккаунтов пользователя",
            description = "Обновляет информацию об аккаунтах пользователя"
    )
    @ApiResponse(responseCode = "200", description = "Информация об аккаунтах успешно обновлена")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PostMapping("/{login}/editUserAccounts")
    public Mono<User> editUserAccounts(
            @PathVariable
            @Parameter(description = "Логин пользователя")
            String login,
            @RequestBody @Valid
            @Parameter(description = "Обновленная информация о пользователе")
            User user
    ) {
        log.info("User {} requested to edit his accounts", login);
        return userService.updateUserAccounts(login, user);
    }
}