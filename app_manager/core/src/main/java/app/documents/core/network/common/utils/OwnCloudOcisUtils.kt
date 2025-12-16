package app.documents.core.network.common.utils

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.Storage
import app.documents.core.network.OWNCLOUD_CLIENT_ID
import app.documents.core.network.OWNCLOUD_REDIRECT_SUFFIX

object OwnCloudOcisUtils {
    val storage = Storage(
        name = ApiContract.Storage.OWNCLOUD,
        clientId = OWNCLOUD_CLIENT_ID,
        redirectUrl = OWNCLOUD_REDIRECT_SUFFIX
    )
}