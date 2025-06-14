package ru.yandex.practicum.transfer.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.transfer.service.request.TransferRequest;
import ru.yandex.practicum.transfer.service.service.TransferService;

@RestController
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/users/{login}/transfer")
    public Mono<Void> transfer(@RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}