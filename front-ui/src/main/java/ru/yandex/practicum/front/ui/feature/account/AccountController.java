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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class AccountController {

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(value = {"/main", "/"})
    public Mono<String> getAccountsInfo(@AuthenticationPrincipal AccountDetails userDetails,
                                        Model model) {
        if (userDetails == null) {
            return Mono.just("main.html");
        }
        return accountClient.getAccountDetails(userDetails.getUsername())
                .flatMap(user -> {
                    model.addAttribute("login", user.getLogin());
                    model.addAttribute("name", user.getName());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    String formattedDate = user.getBirthdate().format(formatter);
                    model.addAttribute("birthdate", formattedDate);
                    model.addAttribute("accounts", user.getAccounts());

                    return Mono.just("main.html");
                });
    }

    @GetMapping(value = "/signup")
    public Mono<String> getSignUpForm() {
        return Mono.just("signup.html");
    }

    @PostMapping(value = "/signup")
    public Mono<String> registerUser(Model model, CreateUserRequest request) {
        return accountClient.createUser(request)
                .flatMap(user -> Mono.just("redirect:/login"))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    try {
                        ErrorResponse error = objectMapper.readValue(
                                ex.getResponseBodyAsString(),
                                ErrorResponse.class
                        );
                        model.addAttribute("errors", List.of(error.error()));
                        model.addAttribute("login", request.getLogin());
                        model.addAttribute("name", request.getName());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        model.addAttribute("birthdate", request.getBirthdate());
                        return Mono.just("signup.html");
                    } catch (IOException e) {
                        return Mono.error(ex);
                    }
                });
    }

    @PostMapping(value = "/user/{login}/editPassword")
    public Mono<String> editPassword(@AuthenticationPrincipal AccountDetails userDetails,
                                     Model model,
                                     @PathVariable("login") String login,
                                     EditPasswordRequest request) {
        return accountClient.editPassword(login, request)
                .flatMap(user -> Mono.just("redirect:/main"))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    try {
                        ErrorResponse error = objectMapper.readValue(
                                ex.getResponseBodyAsString(),
                                ErrorResponse.class
                        );
                        model.addAttribute("passwordErrors", List.of(error.error()));
                        return getAccountsInfo(userDetails, model);
                    } catch (IOException e) {
                        return Mono.error(ex);
                    }
                });
    }

    @PostMapping(value = "/user/{login}/editUserAccounts")
    public Mono<String> editUserAccounts(@AuthenticationPrincipal AccountDetails userDetails,
                                         Model model,
                                         @PathVariable("login") String login,
                                         UserUpdateRequest request) {
        return accountClient.getAccountDetails(login)
                .flatMap(account -> {
                    List<Account> accounts = account.getAccounts()
                            .stream()
                            .peek(acc -> acc.setExists(request.getAccount().contains(acc.getCurrency().getName())))
                            .toList();
                    var newBirthday = request.getBirthdate() != null && !request.getBirthdate().isEmpty()
                            ? LocalDate.parse(request.getBirthdate()).atStartOfDay().atOffset(ZoneOffset.UTC)
                            : account.getBirthdate();
                    User user = User.builder()
                            .id(account.getId())
                            .login(login)
                            .name(request.getName())
                            .password(account.getPassword())
                            .birthdate(newBirthday)
                            .accounts(accounts)
                            .build();
                    return Mono.just(user);
                })
                .flatMap(user -> accountClient.editUserAccounts(login, user))
                .flatMap(user -> Mono.just("redirect:/main"))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    try {
                        ErrorResponse error = objectMapper.readValue(
                                ex.getResponseBodyAsString(),
                                ErrorResponse.class
                        );
                        model.addAttribute("userAccountsErrors", List.of(error.error()));
                        return getAccountsInfo(userDetails, model);
                    } catch (IOException e) {
                        return Mono.error(ex);
                    }
                });
    }
}
