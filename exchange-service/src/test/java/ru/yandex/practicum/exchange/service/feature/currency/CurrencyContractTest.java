package ru.yandex.practicum.exchange.service.feature.currency;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CurrencyContractTest {

    @Autowired
    CurrencyController currencyController;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.standaloneSetup(currencyController);
    }
}

