package app.editors.manager.storages.onedrive.mvp.models.request

import app.editors.manager.storages.onedrive.mvp.models.other.Item
import kotlinx.serialization.Serializable


@Serializable
data class UploadRequest(
    val item: Item? = null
)
