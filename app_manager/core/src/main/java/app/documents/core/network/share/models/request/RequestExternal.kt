package app.documents.core.network.share.models.request

import app.documents.core.model.cloud.Access
import kotlinx.serialization.Serializable

@Serializable
data class RequestExternal(val share: Int = Access.None.code)