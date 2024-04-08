package app.documents.core.network.share.models.request

import app.documents.core.network.share.models.ExternalLink
import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.TimeUtils

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
                expirationDate = TimeUtils.parseDate(sharedLink.sharedTo.expirationDate)
                    ?.let(TimeUtils.DEFAULT_FORMAT::format),
                linkId = sharedLink.sharedTo.id,
                primary = true
            )
        }
    }
}