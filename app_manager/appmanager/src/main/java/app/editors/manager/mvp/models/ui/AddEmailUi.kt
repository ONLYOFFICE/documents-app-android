package app.editors.manager.mvp.models.ui

import app.documents.core.network.manager.models.base.ItemProperties
import app.editors.manager.R

class AddEmailUi : ItemProperties(), ShareViewType {

    override val itemId: String
        get() = ""

    override val itemName: String
        get() = ""

    override val viewType: Int
        get() = R.layout.add_item_layout

}