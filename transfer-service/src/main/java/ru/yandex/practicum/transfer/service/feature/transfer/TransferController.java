package ru.yandex.practicum.transfer.service.feature.transfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping("/users/{login}/transfer")
    public Mono<Void> transfer(@RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}