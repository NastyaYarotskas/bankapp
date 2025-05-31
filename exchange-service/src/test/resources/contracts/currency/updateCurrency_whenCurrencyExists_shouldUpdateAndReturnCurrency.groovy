package contracts.currency

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should update currency when it exists"

    request {
        method PUT()
        url value(consumer(regex('/api/currencies/(USD|CNY|RUB)')))
        headers {
            contentType(applicationJson())
        }
        body([
                name: $(c(regex('(USD|CNY|RUB)')), p('USD')),
                title: $(c(regex('(Dollars|Yuan|Rubles)')), p('Dollars')),
                value: $(anyDouble())
        ])
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
                name: fromRequest().body('$.name'),
                title: fromRequest().body('$.title'),
                value: fromRequest().body('$.value')
        ])
        bodyMatchers {
            jsonPath('$.name', byRegex(nonEmpty()))
            jsonPath('$.title', byRegex(nonEmpty()))
            jsonPath('$.value', byRegex(number()))
        }
    }
}