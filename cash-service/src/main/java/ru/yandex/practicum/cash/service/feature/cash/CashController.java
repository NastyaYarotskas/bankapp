package ru.yandex.practicum.cash.service.feature.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
