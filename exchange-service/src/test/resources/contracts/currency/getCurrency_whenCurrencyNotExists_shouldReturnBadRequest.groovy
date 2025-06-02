package contracts.currency

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request when currency doesn't exist"

    request {
        method GET()
        url "/api/currencies/EURO"
        headers {
            accept applicationJson()
        }
    }

    response {
        status BAD_REQUEST()
    }
}