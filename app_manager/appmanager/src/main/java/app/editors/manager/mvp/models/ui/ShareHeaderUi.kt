package app.editors.manager.mvp.models.ui

import app.editors.manager.R
import lib.toolkit.base.ui.adapters.holder.ViewType

data class ShareHeaderUi(val title: String) : ViewType {
    override val viewType: Int
        get() = R.layout.list_share_settings_header
}