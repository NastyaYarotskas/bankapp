package contracts.currency

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return all currencies"

    request {
        method GET()
        url "/api/currencies"
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
                [
                        name: "USD",
                        title: "Dollars",
                        value: $(anyDouble())
                ],
                [
                        name: "CNY",
                        title: "Yuan",
                        value: $(anyDouble())
                ],
                [
                        name: "RUB",
                        title: "Rubles",
                        value: $(anyDouble())
                ]
        ])
        bodyMatchers {
            jsonPath('$', byType())
            jsonPath('$[*]', byType())
            jsonPath('$[*].name', byRegex(nonEmpty()))
            jsonPath('$[*].title', byRegex(nonEmpty()))
            jsonPath('$[*].value', byRegex(number()))
        }
    }
}