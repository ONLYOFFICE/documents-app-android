package app.documents.core.network.share.models.response

import app.documents.core.network.common.models.Base
import kotlinx.serialization.Serializable


@Serializable
data class ResponseGroups(val response: List<app.documents.core.network.login.models.Group> = emptyList()) : Base()