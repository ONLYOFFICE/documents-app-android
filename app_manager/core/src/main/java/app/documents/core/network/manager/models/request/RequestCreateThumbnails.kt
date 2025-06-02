package app.documents.core.network.manager.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateThumbnails(
    val folderIds: List<String> = ArrayList(),
    val fileIds: List<String> = ArrayList()
)