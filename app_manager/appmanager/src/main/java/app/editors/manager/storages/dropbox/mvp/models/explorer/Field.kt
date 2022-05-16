package app.editors.manager.storages.dropbox.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class Field(
    val name: String = "",
    val value: String = ""
)
