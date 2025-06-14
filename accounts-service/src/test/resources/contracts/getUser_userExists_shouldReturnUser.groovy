package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user when user exists"

    request {
        method GET()
        url "/api/v1/users/test_edit_user_login"
    }

    response {
        status OK()
        headers {
            contentType('application/json')
        }
        body([
                id: anyUuid(),
                login: "test_edit_user_login",
                accounts: [
                        [
                                id: anyUuid(),
                                currency: [
                                        title: "Dollars",
                                        name: "USD"
                                ],
                                value: 1000,
                                exists: true
                        ],
                        [
                                id: anyUuid(),
                                currency: [
                                        title: "Rubles",
                                        name: "RUB"
                                ],
                                value: 1000,
                                exists: true
                        ],
                        [
                                id: anyUuid(),
                                currency: [
                                        title: "Yuan",
                                        name: "CNY"
                                ],
                                value: 1000,
                                exists: true
                        ]
                ]
        ])
        bodyMatchers {
            jsonPath('$.accounts', byType {
                minOccurrence(3)
            })
        }
    }
}