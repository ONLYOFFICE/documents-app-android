package app.documents.core.network.models.login.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestNumber(
    val mobilePhone: String,
) : RequestSignIn()