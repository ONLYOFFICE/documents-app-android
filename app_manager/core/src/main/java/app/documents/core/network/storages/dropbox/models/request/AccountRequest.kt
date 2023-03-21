package app.documents.core.network.storages.dropbox.models.request

import kotlinx.serialization.Serializable

@Serializable
data class AccountRequest(
    val account_id: String = ""
)
