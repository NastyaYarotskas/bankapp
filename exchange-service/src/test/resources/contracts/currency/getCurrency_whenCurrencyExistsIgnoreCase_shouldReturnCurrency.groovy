package contracts.currency

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return currency when it exists (case insensitive)"

    request {
        method GET()
        url "/api/currencies/USD"
        headers {
            accept applicationJson()
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
                name: "USD",
                title: "Dollars",
                value: $(anyDouble())
        ])
        bodyMatchers {
            jsonPath('$.name', byRegex(nonEmpty()))
            jsonPath('$.title', byRegex(nonEmpty()))
            jsonPath('$.value', byRegex(number()))
        }
    }
}