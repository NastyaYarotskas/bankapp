package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should update user password when request is valid"

    request {
        method POST()
        url "/api/v1/users/edit_password_user/editPassword"
        headers {
            contentType('application/json')
        }
        body([
                password: "new_password",
                confirmPassword: "new_password"
        ])
    }

    response {
        status OK()
        headers {
            contentType('application/json')
        }
        body([
                id: regex(uuid()),
                login: "edit_password_user",
                name: "Edit Password User",
                birthdate: anyIso8601WithOffset(),
                accounts: [
                        [
                                id: regex(uuid()),
                                currency: [
                                        title: anyNonEmptyString(),
                                        name: anyNonEmptyString()
                                ],
                                value: 0,
                                exists: false
                        ],
                        [
                                id: regex(uuid()),
                                currency: [
                                        title: anyNonEmptyString(),
                                        name: anyNonEmptyString()
                                ],
                                value: 0,
                                exists: false
                        ],
                        [
                                id: regex(uuid()),
                                currency: [
                                        title: anyNonEmptyString(),
                                        name: anyNonEmptyString()
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