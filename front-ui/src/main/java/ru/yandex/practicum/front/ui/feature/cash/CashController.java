package ru.yandex.practicum.front.ui.feature.cash;

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
import ru.yandex.practicum.front.ui.feature.error.ErrorResponse;

import java.io.IOException;
import java.util.List;

@Controller
public class CashController {

    @Autowired
    private CashServiceClient cashServiceClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountController accountController;


    @PostMapping(value = "/user/{login}/cash")
    public Mono<String> processAccountTransaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  Model model,
                                                  @PathVariable("login") String login,
                                                  CashChangeRequest request) {
        return cashServiceClient.processAccountTransaction(login, request)
                .then(Mono.fromCallable(() -> "redirect:/main"))
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleAccountTransactionError(ex, userDetails, model));
    }

    private Mono<String> handleAccountTransactionError(WebClientResponseException ex,
                                                       CustomUserDetails userDetails,
                                                       Model model) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(),
                            ErrorResponse.class);
                    model.addAttribute("cashErrors", List.of(error.error()));
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(accountController.getAccountsInfo(userDetails, model));
    }
}
