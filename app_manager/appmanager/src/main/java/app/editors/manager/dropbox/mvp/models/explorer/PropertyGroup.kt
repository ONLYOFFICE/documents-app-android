package app.editors.manager.dropbox.mvp.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class PropertyGroup(
    val template_id: String = "",
    val fields: List<Field> = emptyList()
)
