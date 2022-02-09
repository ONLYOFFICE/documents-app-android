package app.editors.manager.storages.onedrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RenameRequest(
    val name: String = ""
)