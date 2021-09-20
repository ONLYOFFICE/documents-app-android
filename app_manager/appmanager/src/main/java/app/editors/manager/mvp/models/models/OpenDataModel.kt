package app.editors.manager.mvp.models.models

import kotlinx.serialization.Serializable

@Serializable
data class OpenDataModel(
    val portal: String? = null,
    val email: String? = null,
    val file: OpenFileModel? = null,
    val folder: OpenFolderModel? = null
)

@Serializable
data class OpenFileModel(
    val id: Int? = null
)

@Serializable
data class OpenFolderModel(
    val id: Int? = null,
    val parentId: Int? = null,
    val rootFolderType: Int? = null
)