package app.editors.manager.mvp.models.ui

import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties

data class GroupUi(
    val id: String ,
    val name: String,
    val manager: String
): ItemProperties(), ViewType, Comparable<GroupUi> {

    override val type: Int
        get() = R.layout.list_share_add_item

    override fun compareTo(other: GroupUi): Int = id.compareTo(other.id)

}