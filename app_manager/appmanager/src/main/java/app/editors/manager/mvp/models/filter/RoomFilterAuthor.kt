package app.editors.manager.mvp.models.filter

import app.editors.manager.R
import app.editors.manager.ui.views.custom.ChipItem

enum class RoomFilterAuthor(val title: Int, override val withOption: Boolean = false) : ChipItem {
    Me(R.string.item_owner_self),
    OtherUsers(R.string.rooms_filter_author_other_users, true);

    override val chipTitle: Int = title
    override var option: String? = null

    companion object {

        val allTypes: List<RoomFilterAuthor>
            get() = listOf(Me, OtherUsers)
    }
}