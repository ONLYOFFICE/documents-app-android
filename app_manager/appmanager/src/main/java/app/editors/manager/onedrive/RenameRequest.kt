package app.editors.manager.onedrive

import kotlinx.serialization.Serializable

@Serializable
data class RenameRequest(
    val name: String = ""
)