package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestExternal(val share: String = "")