package app.editors.manager.mvp.models.ui

import app.documents.core.network.models.share.SharedTo
import app.editors.manager.R
import lib.toolkit.base.ui.adapters.holder.ViewType

data class ShareUi(
    val access: Int,
    val sharedTo: SharedTo,
    val isLocked: Boolean,
    val isOwner: Boolean,
    val isGuest: Boolean
) : ViewType {

    override val viewType: Int
        get() = R.layout.list_share_settings_item
}