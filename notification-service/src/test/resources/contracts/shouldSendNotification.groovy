package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should send notification and return success message"

    request {
        method POST()
        url "/api/notifications"
        headers {
            contentType applicationJson()
        }
        body([
                login: $(consumer(regex('.+')), producer('testUser')),
                message: $(consumer(regex('.+')), producer('test message'))
        ])
    }

    response {
        status OK()
        headers {
            contentType('text/plain;charset=UTF-8')
        }
        body("Notification sent")
    }
}