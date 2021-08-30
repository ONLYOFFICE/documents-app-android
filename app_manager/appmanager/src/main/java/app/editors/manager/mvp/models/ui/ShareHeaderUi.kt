package app.editors.manager.mvp.models.ui

import app.editors.manager.R

data class ShareHeaderUi(val title: String) : ShareViewType {

    override val itemId: String
        get() = ""

    override val itemName: String
        get() = title

    override val viewType: Int
        get() = R.layout.list_share_add_header
}