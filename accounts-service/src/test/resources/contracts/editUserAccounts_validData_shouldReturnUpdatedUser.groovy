package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should update user accounts when data is valid"

    request {
        method POST()
        url "/api/v1/users/test_edit_user_login/editUserAccounts"
        headers {
            contentType('application/json')
        }
        body([
                id: anyUuid(),
                login: "test_edit_user_login"
        ])
    }

    response {
        status OK()
        headers {
            contentType('application/json')
        }
        body([
                id: anyUuid(),
                login: fromRequest().body('$.login'),
                name: nonEmpty(),
                birthdate: nonEmpty()
        ])
    }
}