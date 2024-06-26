package app.documents.core.model.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRegister(
    var firstName: String = "",
    var email: String = "",
    var lastName: String = "",
    var portalName: String = "",
    var password: String = "",
    var language: String = "",
    var phone: String = "+15115555555",
    var timeZoneName: String = "",
    var recaptchaResponse: String = "",
    var recaptchaType: Int = 1
)