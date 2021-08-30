package app.editors.manager.mvp.models.ui

import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties

data class GroupUi(
    val id: String,
    val name: String,
    val manager: String
): ItemProperties(), ShareViewType, Comparable<GroupUi> {

    override val viewType: Int
        get() = R.layout.list_share_add_item

    override val itemId: String
        get() = id

    override val itemName: String
        get() = name

    override fun compareTo(other: GroupUi): Int = id.compareTo(other.id)
}