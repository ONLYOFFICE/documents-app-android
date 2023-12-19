package app.documents.core.network.room.models

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
    val title: String?
)