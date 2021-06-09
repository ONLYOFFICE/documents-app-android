package app.documents.core.network.models.share.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.login.Group
import kotlinx.serialization.Serializable


@Serializable
data class ResponseGroups(val response: List<Group> = emptyList()) : Base()