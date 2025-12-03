package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class UserPhoto(
    val original: String = "",
    val retina: String = "",
    val max: String = "",
    val big: String = "",
    val medium: String = "",
    val small: String = ""
)