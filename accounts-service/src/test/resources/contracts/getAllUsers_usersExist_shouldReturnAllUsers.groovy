package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return all users"

    request {
        method GET()
        url "/api/v1/users"
    }

    response {
        status OK()
        headers {
            contentType('application/json')
        }
        body([
                [
                        id: regex(uuid()),
                        login: "user1",
                        name: "User One",
                        password: anyNonBlankString(),
                        birthdate: anyIso8601WithOffset()
                ],
                [
                        id: regex(uuid()),
                        login: "user2",
                        name: "User Two",
                        password: anyNonBlankString(),
                        birthdate: anyIso8601WithOffset()
                ]
        ])
    }
}