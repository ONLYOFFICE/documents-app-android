package app.documents.core.network.share.models

import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExternalLink(
    @SerialName("access")
    val accessCode: Int,
    val isLocked: Boolean,
    val isOwner: Boolean,
    val canEditAccess: Boolean,
    val denyDownload: Boolean,
    val sharedTo: ExternalLinkSharedTo
) {

    val access: ApiContract.Access get() = ApiContract.Access.get(accessCode)
}