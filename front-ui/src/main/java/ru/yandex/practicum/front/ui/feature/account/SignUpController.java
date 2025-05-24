package ru.yandex.practicum.front.ui.feature.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.front.ui.feature.account.request.CreateUserRequest;
import ru.yandex.practicum.front.ui.feature.error.ErrorResponse;

import java.io.IOException;

@Controller
public class SignUpController {

    private static final String SIGNUP_VIEW = "signup.html";

    @Autowired
    private AccountsServiceClient accountsServiceClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserModelAttributes userModelAttributes;

    @GetMapping(value = "/signup")
    public Mono<String> getSignUpForm() {
        return Mono.just(SIGNUP_VIEW);
    }

    @PostMapping(value = "/signup")
    public Mono<String> registerUser(Model model, CreateUserRequest request) {
        return accountsServiceClient.createUser(request)
                .map(user -> "redirect:/login")
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleSignupError(ex, model, request));
    }

    private Mono<String> handleSignupError(WebClientResponseException ex, Model model, CreateUserRequest request) {
        return Mono.fromCallable(() -> {
            ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
            userModelAttributes.populateSignupModel(model, request, error);
            return SIGNUP_VIEW;
        }).onErrorResume(IOException.class, e -> Mono.error(ex));
    }
}
