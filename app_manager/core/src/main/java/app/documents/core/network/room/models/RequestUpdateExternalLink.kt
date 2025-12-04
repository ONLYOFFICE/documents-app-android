package app.documents.core.network.room.models

import app.documents.core.network.share.models.ExternalLinkSharedTo
import com.google.gson.annotations.Expose
import kotlinx.serialization.Serializable

@Serializable
data class RequestUpdateExternalLink(
    val access: Int,
    val denyDownload: Boolean,
    val expirationDate: String?,
    @Expose
    val linkId: String? = null,
    val linkType: Int,
    val password: String?,
    val title: String?,
    val internal: Boolean?,
    val primary: Boolean?,
) {
    companion object {
        fun from(sharedLink: ExternalLinkSharedTo, access: Int): RequestUpdateExternalLink {
            return RequestUpdateExternalLink(
                access = access,
                title = sharedLink.title,
                internal = sharedLink.internal == true,
                expirationDate = sharedLink.expirationDate,
                linkId = sharedLink.id,
                primary = true,
                denyDownload = sharedLink.denyDownload,
                linkType = sharedLink.linkType,
                password = sharedLink.password
            )
        }
    }
}