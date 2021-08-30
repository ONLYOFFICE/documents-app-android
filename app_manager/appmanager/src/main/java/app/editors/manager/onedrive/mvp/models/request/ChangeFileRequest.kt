package app.editors.manager.onedrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ChangeFileRequest(
    val bytes: ByteArray = byteArrayOf()
)
