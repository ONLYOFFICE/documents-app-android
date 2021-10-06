package app.editors.manager.onedrive.mvp.models.response

data class AuthResponse(
    val token_type: String = "",
    val scope: String = "",
    val expires_in: String = "",
    val ext_expires_in: String = "",
    val access_token: String = "",
    val refresh_token: String = ""
)
