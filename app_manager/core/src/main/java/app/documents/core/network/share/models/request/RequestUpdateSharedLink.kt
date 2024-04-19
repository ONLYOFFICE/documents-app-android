package app.documents.core.network.share.models.request

import app.documents.core.network.share.models.ExternalLink
import kotlinx.serialization.Serializable

@Serializable
data class RequestUpdateSharedLink(
    val access: Int,
    val internal: Boolean,
    val primary: Boolean,
    val expirationDate: String? = null,
    val linkId: String? = null
) {
    companion object {

        fun from(sharedLink: ExternalLink): RequestUpdateSharedLink {
            return RequestUpdateSharedLink(
                access = sharedLink.access,
                internal = sharedLink.sharedTo.internal == true,
                expirationDate = sharedLink.sharedTo.expirationDate,
                linkId = sharedLink.sharedTo.id,
                primary = true
            )
        }
    }
}