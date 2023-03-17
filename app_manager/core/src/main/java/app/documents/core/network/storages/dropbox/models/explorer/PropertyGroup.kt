package app.documents.core.network.storages.dropbox.models.explorer

import kotlinx.serialization.Serializable


@Serializable
data class PropertyGroup(
    val template_id: String = "",
    val fields: List<Field> = emptyList()
)
