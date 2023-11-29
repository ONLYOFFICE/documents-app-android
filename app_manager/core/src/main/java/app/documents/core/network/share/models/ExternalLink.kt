package app.documents.core.network.share.models

import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.SerialName

data class ExternalLink(
    @SerialName("access")
    val accessCode: Int,
    val isLocked: Boolean,
    val isOwner: Boolean,
    val canEditAccess: Boolean,
    val denyDownload: Boolean,
    val sharedTo: ExternalLinkSharedTo,
    val expirationDate: String? // "2023-12-08T00:00:00.0000000+03:00"
) {

    val access: ApiContract.Access get() = ApiContract.Access.get(accessCode)
}