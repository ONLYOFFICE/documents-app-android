package app.documents.core.network.models.share.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestExternalAccess(val share: Int = 0)