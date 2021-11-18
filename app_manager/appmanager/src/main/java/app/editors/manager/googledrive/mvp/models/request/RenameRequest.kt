package app.editors.manager.googledrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RenameRequest(
    val name: String = ""
)
