package app.documents.core.network.share.models

interface ShareEntity {
    val canEditAccess: Boolean
    val isOwner: Boolean
    val sharedTo: SharedTo
    val accessCode: Int
}