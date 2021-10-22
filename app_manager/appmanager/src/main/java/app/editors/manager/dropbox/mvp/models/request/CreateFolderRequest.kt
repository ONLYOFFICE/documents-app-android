package app.editors.manager.dropbox.mvp.models.request

import kotlinx.serialization.Serializable


@Serializable
data class CreateFolderRequest(
    val path: String = "",
    val autorename: Boolean = false
)
