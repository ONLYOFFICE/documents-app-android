package app.documents.core.network.models.share

import app.documents.core.network.ApiContract

import kotlinx.serialization.Serializable


@Serializable
class Share(
    val access: Int = ApiContract.ShareCode.NONE,
    val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    val isOwner: Boolean = false
)