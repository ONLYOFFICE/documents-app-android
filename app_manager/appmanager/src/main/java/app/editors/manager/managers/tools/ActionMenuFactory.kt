package app.editors.manager.managers.tools

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.ApiContract.Parameters
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import lib.toolkit.base.ui.popup.IActionMenuItem

sealed class ActionMenuItem(override val title: Int) : IActionMenuItem {

    class TopBar(title: Int) : ActionMenuItem(title = title)
    data object Divider : ActionMenuItem(title = -1)
    sealed class Arrow(title: Int, val items: List<ActionMenuItem> = listOf()) : ActionMenuItem(title = title)
    sealed class None(title: Int) : ActionMenuItem(title = title)

    sealed class Sort(
        title: Int,
        val sortValue: String,
        var active: Boolean = false,
        var asc: Boolean = false,
    ) : ActionMenuItem(title = title) {

        fun get(asc: Boolean, sortBy: String?): Sort {
            return apply {
                this.asc = asc
                active = sortBy == sortValue
            }
        }
    }

    sealed class Operation(title: Int, val value: OperationsState.OperationType) : None(title)

    data object Select : None(R.string.toolbar_menu_main_select)
    data object SelectAll : None(R.string.toolbar_menu_main_select_all)
    data object Deselect : None(R.string.toolbar_menu_main_deselect)
    data object EmptyTrash : None(R.string.trash_dialog_empty_title)
    data object Download : None(R.string.toolbar_menu_main_download)
    data object Move : Operation(R.string.toolbar_menu_main_move, OperationsState.OperationType.MOVE)
    data object Copy : Operation(R.string.toolbar_menu_main_copy, OperationsState.OperationType.COPY)
    data object Restore : Operation(R.string.device_trash_files_restore, OperationsState.OperationType.RESTORE)
    data object Delete : Operation(R.string.list_context_delete, OperationsState.OperationType.DELETE)
    data object Date : Sort(R.string.toolbar_menu_sort_date_modified, sortValue = Parameters.VAL_SORT_BY_UPDATED)
    data object Title : Sort(R.string.toolbar_menu_sort_title, sortValue = Parameters.VAL_SORT_BY_TITLE)
    data object Type : Sort(R.string.toolbar_menu_sort_type, sortValue = Parameters.VAL_SORT_BY_TYPE)
    data object Size : Sort(R.string.toolbar_menu_sort_size, sortValue = Parameters.VAL_SORT_BY_SIZE)
    data object Author : Sort(R.string.filter_title_author, sortValue = Parameters.VAL_SORT_BY_OWNER)
    data object RoomTags : Sort(R.string.toolbar_menu_sort_tags, sortValue = Parameters.VAL_SORT_BY_TAGS)
    data object RoomType : Sort(R.string.toolbar_menu_sort_type, sortValue = Parameters.VAL_SORT_BY_ROOM_TYPE)
}

object ActionMenuItemsFactory {

    fun getDocsItems(
        section: Int,
        selected: Boolean = false,
        allSelected: Boolean = false,
        asc: Boolean = false,
        sortBy: String? = null,
    ): List<ActionMenuItem> =
        mutableListOf<ActionMenuItem>().apply {
            // select block
            if (!selected) {
                add(ActionMenuItem.Select)
            } else {
                add(ActionMenuItem.Deselect)
            }
            if (!allSelected) add(ActionMenuItem.SelectAll)
            add(ActionMenuItem.Divider)

            if (!selected) {
                // empty trash
                if (section == ApiContract.SectionType.CLOUD_TRASH) {
                    add(ActionMenuItem.EmptyTrash)
                    add(ActionMenuItem.Divider)
                }

                // sort block
                addAll(
                    listOfNotNull(
                        ActionMenuItem.Title,
                        ActionMenuItem.Type,
                        ActionMenuItem.Size,
                        ActionMenuItem.Author.takeIf { section != ApiContract.SectionType.DEVICE_DOCUMENTS },
                        ActionMenuItem.Date
                    ).map { it.get(asc, sortBy) }
                )
            } else if (section == ApiContract.SectionType.CLOUD_TRASH) {
                // trash action block
                add(ActionMenuItem.Restore)
                add(ActionMenuItem.Delete)
            } else {
                // action block
                if (section != ApiContract.SectionType.DEVICE_DOCUMENTS) add(ActionMenuItem.Download)
                add(ActionMenuItem.Move)
                add(ActionMenuItem.Copy)
                add(ActionMenuItem.Delete)
            }
        }

    //    listOfNotNull(
    //    ActionMenuItem.Operation.Restore().takeIf
    //    {
    //        section in arrayOf(
    //            ApiContract.SectionType.CLOUD_TRASH,
    //            ApiContract.SectionType.CLOUD_ARCHIVE_ROOM
    //        )
    //    },
    //    ActionMenuItem.Operation.Delete().takeIf
    //    { section == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM },
    //    ActionMenuItem.Deselect(),
    //    ActionMenuItem.SelectAll(),
    //    ActionMenuItem.Download(),
    //    ActionMenuItem.Operation.Move(),
    //    ActionMenuItem.Operation.Copy()
    //    )
    //
    //    private val selectPopupItems: List<MainPopupItem> = listOf(
    //        MainPopupItem.Select,
    //        MainPopupItem.SelectAll
    //    )
    //
    //    private val roomSortPopupItems: List<MainPopupItem> = listOf(
    //        MainPopupItem.SortBy.Date,
    //        MainPopupItem.SortBy.Title,
    //        MainPopupItem.SortBy.RoomType,
    //        MainPopupItem.SortBy.RoomTags,
    //        MainPopupItem.SortBy.Author
    //    )
    //
    //    private val sortPopupItems: List<MainPopupItem> = listOf(
    //        MainPopupItem.SortBy.Date,
    //        MainPopupItem.SortBy.Title,
    //        MainPopupItem.SortBy.Type,
    //        MainPopupItem.SortBy.Size,
    //        MainPopupItem.SortBy.Author
    //    )
    //
    //    fun getItems(section: Int): MutableList<MainPopupItem> =
    //        mutableListOf<MainPopupItem>().apply {
    //            if (ApiContract.SectionType.isRoom(section)) {
    //                add(MainPopupItem.TestArrowItem)
    //                addAll(MainPopup.selectPopupItems)
    //                addAll(MainPopup.roomSortPopupItems)
    //            } else {
    //                addAll(MainPopup.selectPopupItems)
    //                if (section == ApiContract.SectionType.CLOUD_TRASH) add(MainPopupItem.EmptyTrash)
    //                addAll(MainPopup.sortPopupItems)
    //            }
    //        }
    //
    //    fun getSortPopupItem(sortBy: String?): MainPopupItem {
    //        return when (sortBy) {
    //            Parameters.VAL_SORT_BY_UPDATED -> MainPopupItem.SortBy.Date
    //            Parameters.VAL_SORT_BY_TYPE -> MainPopupItem.SortBy.Type
    //            Parameters.VAL_SORT_BY_SIZE -> MainPopupItem.SortBy.Size
    //            Parameters.VAL_SORT_BY_TITLE -> MainPopupItem.SortBy.Title
    //            Parameters.VAL_SORT_BY_OWNER -> MainPopupItem.SortBy.Author
    //            Parameters.VAL_SORT_BY_ROOM_TYPE -> MainPopupItem.SortBy.RoomType
    //            Parameters.VAL_SORT_BY_TAGS -> MainPopupItem.SortBy.RoomTags
    //            else -> throw NoSuchElementException("There is no such sort type")
    //        }
    //    }
}