package app.documents.core.network.storages.onedrive.models.request

import app.documents.core.network.storages.onedrive.models.other.Item
import kotlinx.serialization.Serializable


@Serializable
data class UploadRequest(
    val item: Item? = null
)
