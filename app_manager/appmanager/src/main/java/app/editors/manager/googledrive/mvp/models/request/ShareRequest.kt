package app.editors.manager.googledrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ShareRequest(
    val role: String = "",
    val type: String = ""
)
