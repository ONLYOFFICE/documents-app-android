package app.documents.core.network.share.models

import androidx.core.text.isDigitsOnly
import app.documents.core.network.common.contracts.ApiContract

import kotlinx.serialization.Serializable


@Serializable
data class Share(
    val access: String = ApiContract.ShareType.NONE,
    val sharedTo: SharedTo = SharedTo(),
    val isLocked: Boolean = false,
    val isOwner: Boolean = false
) {
    val intAccess: Int
    get() {
        return if (access.isDigitsOnly()) {
            access.toInt()
        } else {
            ApiContract.ShareType.getCode(access)
        }
    }
}