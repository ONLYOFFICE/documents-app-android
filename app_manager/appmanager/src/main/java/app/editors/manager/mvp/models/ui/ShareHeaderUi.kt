package app.editors.manager.mvp.models.ui

data class ShareHeaderUi(val title: String) : ShareViewType {

    override val itemId: String
        get() = ""

    override val itemName: String
        get() = title

    override val viewType: Int
        get() = lib.toolkit.base.R.layout.list_item_header
}