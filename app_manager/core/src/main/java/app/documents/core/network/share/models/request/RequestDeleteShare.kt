package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestDeleteShare(
    val folderIds: List<String> = emptyList(),
    val fileIds: List<String> = emptyList()
)