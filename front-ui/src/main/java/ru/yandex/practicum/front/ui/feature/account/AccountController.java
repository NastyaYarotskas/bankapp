package ru.yandex.practicum.front.ui.feature.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Controller
public class AccountController {

    private static final String MAIN_VIEW = "main.html";
    private static final String SIGNUP_VIEW = "signup.html";

    @Autowired
    private AccountClient accountClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserModelAttributes userModelAttributes;

    @GetMapping(value = {"/main", "/"})
    public Mono<String> getAccountsInfo(@AuthenticationPrincipal AccountDetails userDetails,
                                        Model model) {
        if (userDetails == null) {
            return Mono.just(MAIN_VIEW);
        }

        return accountClient.getAccountDetails(userDetails.getUsername())
                .map(user -> {
                    userModelAttributes.populateUserAttributes(model, user);
                    return MAIN_VIEW;
                });
    }

    @GetMapping(value = "/signup")
    public Mono<String> getSignUpForm() {
        return Mono.just(SIGNUP_VIEW);
    }

    @PostMapping(value = "/signup")
    public Mono<String> registerUser(Model model, CreateUserRequest request) {
        return accountClient.createUser(request)
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

    @PostMapping(value = "/user/{login}/editPassword")
    public Mono<String> editPassword(@AuthenticationPrincipal AccountDetails userDetails,
                                     Model model,
                                     @PathVariable("login") String login,
                                     EditPasswordRequest request) {
        return accountClient.editPassword(login, request)
                .map(user -> "redirect:/main")
                .onErrorResume(WebClientResponseException.class,
                        exception -> handleEditPasswordError(exception, userDetails, model));

    }

    private Mono<String> handleEditPasswordError(WebClientResponseException ex, AccountDetails userDetails, Model model) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(), ErrorResponse.class);
                    model.addAttribute("passwordErrors", List.of(error.error()));
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(getAccountsInfo(userDetails, model));
    }

    @PostMapping(value = "/user/{login}/editUserAccounts")
    public Mono<String> editUserAccounts(@AuthenticationPrincipal AccountDetails userDetails,
                                         Model model,
                                         @PathVariable("login") String login,
                                         UserUpdateRequest request) {
        return accountClient.getAccountDetails(login)
                .map(account -> updateUserFromRequest(account, request, login))
                .flatMap(user -> accountClient.editUserAccounts(login, user))
                .map(user -> "redirect:/main")
                .onErrorResume(WebClientResponseException.class,
                        ex -> handleEditAccountsError(ex, userDetails, model));
    }

    private User updateUserFromRequest(User account, UserUpdateRequest request, String login) {
        List<Account> updatedAccounts = updateAccounts(account.getAccounts(), request.getAccount());
        OffsetDateTime newBirthday = parseBirthdate(request.getBirthdate(), account.getBirthdate());

        return User.builder()
                .id(account.getId())
                .login(login)
                .name(request.getName())
                .password(account.getPassword())
                .birthdate(newBirthday)
                .accounts(updatedAccounts)
                .build();
    }

    private List<Account> updateAccounts(List<Account> accounts, List<String> selectedAccounts) {
        return accounts.stream()
                .peek(acc -> acc.setExists(selectedAccounts.contains(acc.getCurrency().getName())))
                .toList();
    }

    private OffsetDateTime parseBirthdate(String birthdate, OffsetDateTime defaultBirthdate) {
        return birthdate != null && !birthdate.isEmpty()
                ? LocalDate.parse(birthdate).atStartOfDay().atOffset(ZoneOffset.UTC)
                : defaultBirthdate;
    }

    private Mono<String> handleEditAccountsError(WebClientResponseException ex,
                                                 AccountDetails userDetails,
                                                 Model model) {
        return Mono.fromCallable(() -> {
                    ErrorResponse error = objectMapper.readValue(ex.getResponseBodyAsString(),
                            ErrorResponse.class);
                    model.addAttribute("userAccountsErrors", List.of(error.error()));
                    return null;
                })
                .onErrorResume(IOException.class, e -> Mono.error(ex))
                .then(getAccountsInfo(userDetails, model));
    }
}