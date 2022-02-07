package app.editors.manager.mvp.models.models

import kotlinx.serialization.Serializable

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
    val id: String? = null,
    val title: String? = null,
    val extension: String? = null
)

@Serializable
data class OpenFolderModel(
    val id: String? = null,
    val parentId: String? = null,
    val rootFolderType: Int? = null
)