package app.editors.manager.ui.popup

import android.content.Context
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import app.documents.core.network.ApiContract
import app.editors.manager.R
import lib.toolkit.base.databinding.IncludeActionBarItemBinding
import lib.toolkit.base.databinding.PopupActionBarBinding
import lib.toolkit.base.ui.popup.ActionBarPopup
import lib.toolkit.base.ui.popup.ActionBarPopupItem

class MainActionBarPopup(
    context: Context,
    section: Int,
    clickListener: (ActionBarPopupItem) -> Unit,
    private val sortBy: String = "",
    private val isAsc: Boolean = false,
    private val excluded: List<ActionBarPopupItem> = emptyList()
) : ActionBarPopup(context) {

    object Select : ActionBarPopupItem(R.string.toolbar_menu_main_select)
    object SelectAll : ActionBarPopupItem(R.string.toolbar_menu_main_select_all)
    object Date : ActionBarPopupItem(R.string.toolbar_menu_sort_date_modified)
    object Title : ActionBarPopupItem(R.string.toolbar_menu_sort_title)
    object Type : ActionBarPopupItem(R.string.toolbar_menu_sort_type)
    object Size : ActionBarPopupItem(R.string.toolbar_menu_sort_size)
    object Author : ActionBarPopupItem(R.string.filter_title_author)
    object RoomTags : ActionBarPopupItem(R.string.toolbar_menu_sort_tags)
    object RoomType : ActionBarPopupItem(R.string.toolbar_menu_sort_type)

    companion object {
        private val selectPopupItems: List<ActionBarPopupItem> = listOf(Select, SelectAll)
        private val roomSortPopupItems: List<ActionBarPopupItem> = listOf(Date, Title, RoomType, RoomTags, Author)
        val sortPopupItems: List<ActionBarPopupItem> = listOf(Date, Title, Type, Size, Author)

        fun getItems(section: Int): MutableList<ActionBarPopupItem> =
            mutableListOf<ActionBarPopupItem>().apply {
                if (ApiContract.SectionType.isRoom(section)) {
                    addAll(selectPopupItems)
                    addAll(roomSortPopupItems)
                } else {
                    addAll(selectPopupItems)
                    addAll(sortPopupItems)
                }
            }

        fun getSortPopupItem(sortBy: String?): ActionBarPopupItem {
            return when (sortBy) {
                ApiContract.Parameters.VAL_SORT_BY_UPDATED -> Date
                ApiContract.Parameters.VAL_SORT_BY_TYPE -> Type
                ApiContract.Parameters.VAL_SORT_BY_SIZE -> Size
                ApiContract.Parameters.VAL_SORT_BY_TITLE -> Title
                ApiContract.Parameters.VAL_SORT_BY_OWNER -> Author
                ApiContract.Parameters.VAL_SORT_BY_ROOM_TYPE -> RoomType
                ApiContract.Parameters.VAL_SORT_BY_TAGS -> RoomTags
                else -> throw NoSuchElementException("There is no such sort type")
            }
        }

    }

    private var viewBinding: PopupActionBarBinding? = null

    init {
        val items = getItems(section)
        viewBinding = PopupActionBarBinding.inflate(inflater).apply {
            divider.isVisible = !excluded.containsAll(selectPopupItems) && !excluded.containsAll(sortPopupItems)
            popupMenu.children
                .filterIsInstance(LinearLayout::class.java)
                .forEachIndexed { index, view ->
                    with(IncludeActionBarItemBinding.bind(view)) {
                        popupMenuText.text = context.getText(items[index].title)
                        popupMenuOrder.isSelected = isAsc
                        popupMenuOrder.isVisible = getSortPopupItem(sortBy) == items[index]
                        popupMenuItem.isVisible = excluded.contains(items[index]) != true
                        popupMenuItem.setOnClickListener {
                            clickListener(items[index])
                            hide()
                        }
                    }
                }
        }.also {
            contentView = it.root
        }
    }

    override fun hide() {
        super.hide()
        viewBinding = null
    }
}