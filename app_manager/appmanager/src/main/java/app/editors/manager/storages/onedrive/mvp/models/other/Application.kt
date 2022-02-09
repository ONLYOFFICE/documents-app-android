package app.editors.manager.storages.onedrive.mvp.models.other

import kotlinx.serialization.Serializable


@Serializable
data class Application(
    val id: String = "",
    val displayName: String = ""
)
