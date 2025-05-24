package ru.yandex.practicum.front.ui.feature.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.account.AccountController;
import ru.yandex.practicum.front.ui.feature.auth.CustomUserDetails;
import ru.yandex.practicum.front.ui.feature.cash.CashChangeRequest;
import ru.yandex.practicum.front.ui.feature.cash.CashServiceClient;
import ru.yandex.practicum.front.ui.feature.error.ErrorResponse;

import java.io.IOException;
import java.util.List;

@Controller
public class TransferController {

    @Autowired
    private TransferServiceClient transferServiceClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountController accountController;

    @PostMapping(value = "/user/{login}/transfer")
    public Mono<String> transfer(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model,
                                 @PathVariable("login") String login,
                                 TransferRequest request) {
        return transferServiceClient.transfer(login, request)
                .then(Mono.fromCallable(() -> "redirect:/main"))
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleTransferError(ex, userDetails, model, request));
    }

    private Mono<String> handleTransferError(WebClientResponseException ex,
                                             CustomUserDetails userDetails,
                                             Model model,
                                             TransferRequest request) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(),
                            ErrorResponse.class);
                    if (request.getLogin().equals(request.getToLogin())) {
                        model.addAttribute("transferErrors", List.of(error.error()));
                    } else {
                        model.addAttribute("transferOtherErrors", List.of(error.error()));
                    }
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(accountController.getAccountsInfo(userDetails, model));
    }
}
