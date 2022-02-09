package app.editors.manager.storages.dropbox.mvp.models.request

import kotlinx.serialization.Serializable


@Serializable
data class CreateFolderRequest(
    val path: String = "",
    val autorename: Boolean = false
)
