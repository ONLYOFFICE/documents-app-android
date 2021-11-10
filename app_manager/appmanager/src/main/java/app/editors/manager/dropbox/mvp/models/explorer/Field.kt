package app.editors.manager.dropbox.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class Field(
    val name: String = "",
    val value: String = ""
)
