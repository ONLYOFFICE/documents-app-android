package app.documents.core.network.models.share.response

import app.documents.core.network.models.Base
import app.documents.core.network.models.share.Invite
import kotlinx.serialization.Serializable

@Serializable
data class ResponseInvite(val response: List<Invite> = emptyList()) : Base()