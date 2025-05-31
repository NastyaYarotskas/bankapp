package contracts.currency

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return bad request when trying to update non-existent currency"

    request {
        method PUT()
        url "/api/currencies/EURO"
        headers {
            contentType(applicationJson())
        }
        body([
                name: "EURO",
                title: "Euro",
                value: 0.85
        ])
    }

    response {
        status BAD_REQUEST()
    }
}