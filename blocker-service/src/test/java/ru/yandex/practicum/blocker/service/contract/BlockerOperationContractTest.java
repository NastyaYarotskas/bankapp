package ru.yandex.practicum.blocker.service.contract;

import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.blocker.service.config.TestOAuth2ClientConfig;
import ru.yandex.practicum.blocker.service.controller.BlockerOperationController;
import ru.yandex.practicum.blocker.service.model.OperationCheckResult;
import ru.yandex.practicum.blocker.service.model.OperationContext;
import ru.yandex.practicum.blocker.service.service.BlockerOperationService;

@SpringBootTest
@Import(TestOAuth2ClientConfig.class)
public class BlockerOperationContractTest {

    @MockitoBean
    BlockerOperationService blockerOperationService;

    @Autowired
    BlockerOperationController blockerOperationController;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.standaloneSetup(blockerOperationController);

        Mockito.when(blockerOperationService.checkOperation(Mockito.any()))
                .thenAnswer((invocationOnMock) -> {
                    OperationContext request = invocationOnMock.getArgument(0);
                    if (request.getAmount() > 10000) {
                        return Mono.just(new OperationCheckResult(
                                true,
                                "Operation blocked by Large Amount Detector",
                                "LARGE_AMOUNT_DETECTOR"
                        ));
                    }
                    return Mono.just(OperationCheckResult.ok());
                });
    }
}
