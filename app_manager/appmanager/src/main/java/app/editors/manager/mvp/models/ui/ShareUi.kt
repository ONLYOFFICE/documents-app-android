package app.editors.manager.mvp.models.ui

import app.documents.core.network.models.share.SharedTo
import app.editors.manager.R

data class ShareUi(
    val access: Int,
    val sharedTo: SharedTo,
    val isLocked: Boolean,
    val isOwner: Boolean
) : ViewType {
    override val type: Int
        get() = R.layout.list_share_settings_item
}