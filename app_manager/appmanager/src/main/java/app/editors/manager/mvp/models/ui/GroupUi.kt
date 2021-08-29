package app.editors.manager.mvp.models.ui

import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties
import lib.toolkit.base.ui.adapters.holder.ViewType

data class GroupUi(
    val id: String,
    val name: String,
    val manager: String
): ItemProperties(), ViewType, Comparable<GroupUi> {

    override val viewType: Int
        get() = R.layout.list_share_add_item

    override fun getItemId(): String {
        return id
    }

    override fun getItemName(): String {
        return name
    }

    override fun compareTo(other: GroupUi): Int = id.compareTo(other.id)

}