package ru.yandex.practicum.cash.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cash.service.request.CashChangeRequest;
import ru.yandex.practicum.cash.service.service.CashService;

@RestController
@RequestMapping("/api/v1/users")
public class CashController {

    @Autowired
    private CashService cashService;

    @PostMapping("/{login}/cash")
    public Mono<Void> processAccountTransaction(@PathVariable String login, @RequestBody CashChangeRequest request) {
        return cashService.processAccountTransaction(login, request);
    }
}
