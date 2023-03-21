package app.editors.manager.ui.popup

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import lib.toolkit.base.databinding.ActionPopupItemListBinding
import lib.toolkit.base.ui.adapters.factory.inflate
import lib.toolkit.base.ui.popup.ActionBarPopup
import lib.toolkit.base.ui.popup.BasePopupItem

sealed class MainPopupItem(
    title: Int,
    withDivider: Boolean = false
) : BasePopupItem(title, withDivider) {

    object Select : MainPopupItem(R.string.toolbar_menu_main_select)
    object SelectAll : MainPopupItem(R.string.toolbar_menu_main_select_all, true)
    object EmptyTrash : MainPopupItem(R.string.trash_dialog_empty_title, true)

    sealed class SortBy(
        title: Int,
        val value: String,
        withDivider: Boolean = false
    ) : MainPopupItem(title, withDivider) {
        object Date : SortBy(R.string.toolbar_menu_sort_date_modified, ApiContract.Parameters.VAL_SORT_BY_UPDATED)
        object Title : SortBy(R.string.toolbar_menu_sort_title, ApiContract.Parameters.VAL_SORT_BY_TITLE)
        object Type : SortBy(R.string.toolbar_menu_sort_type, ApiContract.Parameters.VAL_SORT_BY_TYPE)
        object Size : SortBy(R.string.toolbar_menu_sort_size, ApiContract.Parameters.VAL_SORT_BY_SIZE)
        object Author : SortBy(R.string.filter_title_author, ApiContract.Parameters.VAL_SORT_BY_OWNER)
        object RoomTags : SortBy(R.string.toolbar_menu_sort_tags, ApiContract.Parameters.VAL_SORT_BY_TAGS)
        object RoomType : SortBy(R.string.toolbar_menu_sort_type, ApiContract.Parameters.VAL_SORT_BY_ROOM_TYPE)
    }
}

class MainPopup(
    context: Context,
    section: Int,
    clickListener: (MainPopupItem) -> Unit,
    sortBy: String,
    isAsc: Boolean,
    excluded: List<MainPopupItem> = emptyList()
) : ActionBarPopup<MainPopupItem>(
    context = context,
    items = getItems(section).filter(excluded),
    clickListener = clickListener
) {

    override val popupAdapter: PopupAdapter<MainPopupItem> = MainPopupAdapter(
        isAsc = isAsc,
        sortBy = getSortPopupItem(sortBy)
    ) {
        clickListener(it)
        dismiss()
    }

    companion object {

        private val selectPopupItems: List<MainPopupItem> = listOf(
            MainPopupItem.Select,
            MainPopupItem.SelectAll
        )

        private val roomSortPopupItems: List<MainPopupItem> = listOf(
            MainPopupItem.SortBy.Date,
            MainPopupItem.SortBy.Title,
            MainPopupItem.SortBy.RoomType,
            MainPopupItem.SortBy.RoomTags,
            MainPopupItem.SortBy.Author
        )

        private val sortPopupItems: List<MainPopupItem> = listOf(
            MainPopupItem.SortBy.Date,
            MainPopupItem.SortBy.Title,
            MainPopupItem.SortBy.Type,
            MainPopupItem.SortBy.Size,
            MainPopupItem.SortBy.Author
        )

        private fun getItems(section: Int): MutableList<MainPopupItem> =
            mutableListOf<MainPopupItem>().apply {
                if (ApiContract.SectionType.isRoom(section)) {
                    addAll(selectPopupItems)
                    addAll(roomSortPopupItems)
                } else {
                    addAll(selectPopupItems)
                    if (section == ApiContract.SectionType.CLOUD_TRASH) add(MainPopupItem.EmptyTrash)
                    addAll(sortPopupItems)
                }
            }

        fun getSortPopupItem(sortBy: String?): MainPopupItem {
            return when (sortBy) {
                ApiContract.Parameters.VAL_SORT_BY_UPDATED -> MainPopupItem.SortBy.Date
                ApiContract.Parameters.VAL_SORT_BY_TYPE -> MainPopupItem.SortBy.Type
                ApiContract.Parameters.VAL_SORT_BY_SIZE -> MainPopupItem.SortBy.Size
                ApiContract.Parameters.VAL_SORT_BY_TITLE -> MainPopupItem.SortBy.Title
                ApiContract.Parameters.VAL_SORT_BY_OWNER -> MainPopupItem.SortBy.Author
                ApiContract.Parameters.VAL_SORT_BY_ROOM_TYPE -> MainPopupItem.SortBy.RoomType
                ApiContract.Parameters.VAL_SORT_BY_TAGS -> MainPopupItem.SortBy.RoomTags
                else -> throw NoSuchElementException("There is no such sort type")
            }
        }

    }

    private open class MainPopupAdapter(
        private val isAsc: Boolean,
        private val sortBy: MainPopupItem,
        clickListener: (MainPopupItem) -> Unit
    ) : PopupAdapter<MainPopupItem>(clickListener) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainPopupItemViewHolder {
            return MainPopupItemViewHolder(
                view = parent.inflate(lib.toolkit.base.R.layout.action_popup_item_list),
                isAsc = isAsc,
                sortBy = sortBy
            )
        }
    }

    private open class MainPopupItemViewHolder(
        private val view: View,
        private val isAsc: Boolean,
        private val sortBy: MainPopupItem
    ) : PopupItemViewHolder<MainPopupItem>(view) {

        override fun bind(item: MainPopupItem, last: Boolean) {
            super.bind(item, last)
            with(ActionPopupItemListBinding.bind(view)) {
                order.isVisible = item == sortBy
                order.isSelected = isAsc
            }
        }
    }
}