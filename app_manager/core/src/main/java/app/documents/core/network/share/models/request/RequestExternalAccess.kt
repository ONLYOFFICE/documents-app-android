package app.documents.core.network.share.models.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestExternalAccess(val share: Int = 0)