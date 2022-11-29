package app.documents.core.network.storages.dropbox.models.request

import kotlinx.serialization.Serializable


@Serializable
data class CreateFolderRequest(
    val path: String = "",
    val autorename: Boolean = false
)
