package app.editors.manager.mvp.models.ui

import app.editors.manager.R

data class ShareHeaderUi(val title: String) : ViewType {
    override val type: Int
        get() = R.layout.list_share_settings_header
}