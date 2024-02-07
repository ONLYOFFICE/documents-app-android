package app.documents.core.network.room.models

import app.documents.core.network.common.contracts.ApiContract
import kotlinx.serialization.Serializable

@Serializable
data class RequestCreateExternalLink(
    val access: Int = ApiContract.ShareCode.READ,
    val denyDownload: Boolean = false,
    val expirationDate: String? = null,
    val linkType: Int = 1,
    val password: String? = null,
    val title: String
)