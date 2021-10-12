package app.editors.manager.dropbox.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class AccountRequest(
    val account_id: String = ""
)
