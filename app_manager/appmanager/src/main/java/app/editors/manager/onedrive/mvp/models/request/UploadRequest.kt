package app.editors.manager.onedrive.mvp.models.request

import app.editors.manager.onedrive.mvp.models.other.Item
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UploadRequest(
    val item: Item? = null
)
