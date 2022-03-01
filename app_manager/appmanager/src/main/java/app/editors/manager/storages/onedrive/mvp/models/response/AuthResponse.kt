package app.editors.manager.storages.onedrive.mvp.models.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token_type: String = "",
    val scope: String = "",
    val expires_in: String = "",
    val ext_expires_in: String = "",
    val access_token: String = "",
    val refresh_token: String = ""
)
