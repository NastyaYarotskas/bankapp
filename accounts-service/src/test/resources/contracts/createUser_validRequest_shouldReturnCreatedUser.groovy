package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create user when sending valid request"

    request {
        method POST()
        url "/api/v1/users"
        headers {
            contentType('application/json')
        }
        body([
                login: "test_user",
                name: "Test User",
                password: "password123",
                confirmPassword: "password123",
                birthdate: "1990-01-01"
        ])
    }

    response {
        status OK()
        headers {
            contentType('application/json')
        }
        body([
                id: regex(uuid()),
                login: "test_user",
                name: "Test User",
                birthdate: anyIso8601WithOffset(),
                accounts: [
                        [
                                id: regex(uuid()),
                                currency: [
                                        title: anyNonEmptyString(),
                                        code: anyNonEmptyString()
                                ],
                                value: 0,
                                exists: false
                        ],
                        [
                                id: regex(uuid()),
                                currency: [
                                        title: anyNonEmptyString(),
                                        code: anyNonEmptyString()
                                ],
                                value: 0,
                                exists: false
                        ],
                        [
                                id: regex(uuid()),
                                currency: [
                                        title: anyNonEmptyString(),
                                        code: anyNonEmptyString()
                                ],
                                value: 0,
                                exists: false
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