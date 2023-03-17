package app.documents.core.network.storages.dropbox.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ExplorerContinueRequest(
    val cursor: String = ""
)
