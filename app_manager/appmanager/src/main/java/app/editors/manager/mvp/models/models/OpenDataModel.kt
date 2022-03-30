package app.editors.manager.mvp.models.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
data class OpenDataModel(
    val portal: String? = null,
    val email: String? = null,
    val file: OpenFileModel? = null,
    val folder: OpenFolderModel? = null,
    val originalUrl: String? = null
)

@Serializable
data class OpenFileModel(
    @Serializable(with = JsonAsStringSerializer::class) val id: String? = null,
    val title: String? = null,
    val extension: String? = null
)

@Serializable
data class OpenFolderModel(
    @Serializable(with = JsonAsStringSerializer::class) val id: String? = null,
    val parentId: String? = null,
    val rootFolderType: Int? = null
)

object JsonAsStringSerializer: JsonTransformingSerializer<String>(tSerializer = String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonPrimitive(value = element.toString())
    }
}