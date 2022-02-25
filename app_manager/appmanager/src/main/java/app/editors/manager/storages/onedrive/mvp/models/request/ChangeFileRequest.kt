package app.editors.manager.storages.onedrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ChangeFileRequest(
    val bytes: ByteArray = byteArrayOf()
)
