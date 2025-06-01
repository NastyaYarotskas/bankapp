package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should allow operation when amount is within limits"

    request {
        method POST()
        url "/api/operations"
        headers {
            contentType applicationJson()
        }
        body(
                userId: $(anyNonEmptyString()),
                operationType: $(anyNonEmptyString()),
                amount: $(regex('[0-9]?[0-9]{0,4}\\.[0-9]+'))
        )
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                blocked: false,
                message: "Operation allowed",
                detectionAlgorithm: null
        )
    }
}