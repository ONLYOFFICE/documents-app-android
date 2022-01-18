package app.editors.manager.mvp.models.ui

import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties

data class GroupUi(
    val id: String,
    var name: String,
    val manager: String
): ItemProperties(), ShareViewType, Comparable<GroupUi> {

    override val viewType: Int
        get() = R.layout.list_share_add_item

    override val itemId: String
        get() = id

    override val itemName: String
        get() = name

    override fun compareTo(other: GroupUi): Int = id.compareTo(other.id)

    companion object {
        const val GROUP_ADMIN_ID = "cd84e66b-b803-40fc-99f9-b2969a54a1de"
        const val GROUP_EVERYONE_ID = "c5cc67d1-c3e8-43c0-a3ad-3928ae3e5b5e"
    }
}