package app.documents.core.network.models.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestRegister(
    var firstName: String = "",
    var email: String = "",
    var lastName: String = "",
    var portalName: String = "",
    var password: String = "",
    var language: String = "",
    var phone: String = "",
    var timeZoneName: String = "",
    var appKey: String = "",
)