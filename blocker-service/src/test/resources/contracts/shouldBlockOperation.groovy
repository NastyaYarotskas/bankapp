package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should block operation when amount is too large"

    request {
        method POST()
        url "/api/operations"
        headers {
            contentType applicationJson()
        }
        body (
                userId: $(anyNonEmptyString()),
                operationType: $(anyNonEmptyString()),
                amount: $(regex('([1-9][0-9]{5,}|[1-9][0-9]{4,}\\.[0-9]+|100000(\\.0+)?)'))
        )
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body (
                blocked: true,
                message: "Operation blocked by Large Amount Detector",
                detectionAlgorithm: "LARGE_AMOUNT_DETECTOR"
        )
    }
}