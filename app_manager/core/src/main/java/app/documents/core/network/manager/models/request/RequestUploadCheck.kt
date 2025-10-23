package app.documents.core.network.manager.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestUploadCheck(
    val filesTitle: List<String> = emptyList()
)