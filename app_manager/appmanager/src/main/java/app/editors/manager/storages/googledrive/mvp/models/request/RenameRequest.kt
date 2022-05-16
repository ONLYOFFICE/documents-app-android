package app.editors.manager.storages.googledrive.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RenameRequest(
    val name: String = ""
)
