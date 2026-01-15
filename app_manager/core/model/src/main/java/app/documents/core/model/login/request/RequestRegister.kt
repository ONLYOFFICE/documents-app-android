package app.documents.core.model.login.request

import kotlinx.serialization.Serializable

//enum RecaptchaType
//{
//    Default = 0,
//    AndroidV2 = 1,
//    iOSV2 = 2,
//    hCaptcha = 3
//}
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
    var recaptchaType: Int = 3
)