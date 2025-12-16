package app.documents.core.network.share.models

import app.documents.core.model.cloud.Access

interface ShareEntity {
    val canEditAccess: Boolean
    val isOwner: Boolean
    val sharedTo: SharedTo
    val access: Access

    val isOwnerOrAdmin: Boolean
        get() = sharedTo.isOwnerOrAdmin
}