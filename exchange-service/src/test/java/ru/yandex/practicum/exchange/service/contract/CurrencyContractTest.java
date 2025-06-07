package ru.yandex.practicum.exchange.service.contract;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.exchange.service.config.TestOAuth2ClientConfig;
import ru.yandex.practicum.exchange.service.controller.CurrencyController;

@SpringBootTest
@Import(TestOAuth2ClientConfig.class)
public class CurrencyContractTest {

    @Autowired
    CurrencyController currencyController;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.standaloneSetup(currencyController);
    }
}

