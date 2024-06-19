package app.editors.manager.managers.tools

import app.documents.core.network.common.contracts.ApiContract.Parameters
import app.documents.core.network.common.contracts.ApiContract.SectionType
import app.documents.core.network.manager.models.explorer.Security
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import lib.toolkit.base.ui.popup.IActionMenuItem

sealed class ActionMenuItem(override val title: Int) : IActionMenuItem {

    class TopBar(title: Int) : ActionMenuItem(title = title)

    data object Divider : ActionMenuItem(title = -1)

    sealed class None(title: Int) : ActionMenuItem(title = title)

    sealed class Arrow(title: Int, var items: List<ActionMenuItem> = listOf()) : ActionMenuItem(title = title) {

        fun get(items: List<ActionMenuItem>): Arrow {
            return apply { this.items = items }
        }
    }

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
    data object Archive : None(R.string.context_room_move_to_archive)
    data object Info : None(R.string.list_context_info)
    data object EditRoom : None(R.string.list_context_edit_room)
    data object Invite : None(R.string.share_invite_user)
    data object LeaveRoom : None(R.string.leave_room_title)
    data class CopyLink(val isRoom: Boolean) : None(R.string.rooms_info_copy_link)

    data object ManageRoom : Arrow(R.string.room_manage_room)
    data object SortBy : Arrow(R.string.toolbar_menu_sort_by)

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

    fun getRoomItems(
        section: Int,
        root: Boolean,
        empty: Boolean,
        currentRoom: Boolean,
        selected: Boolean,
        allSelected: Boolean,
        asc: Boolean,
        security: Security,
        sortBy: String?,
    ): List<ActionMenuItem> {
        return if (root) {
            getRoomRootItems(section, selected, allSelected, asc, sortBy)
        } else {
            getRoomFolderItems(selected, empty, allSelected, asc, sortBy, currentRoom, security)
        }
    }

    fun getDocsItems(
        section: Int,
        selected: Boolean,
        allSelected: Boolean,
        asc: Boolean,
        sortBy: String?,
    ) = mutableListOf<ActionMenuItem>().apply {
        // select block
        if (section != SectionType.LOCAL_RECENT) addAll(getSelectItems(selected, allSelected))
        if (!selected) {
            // empty trash
            if (section == SectionType.CLOUD_TRASH) {
                add(ActionMenuItem.EmptyTrash)
                add(ActionMenuItem.Divider)
            }

            // sort block
            addAll(
                listOfNotNull(
                    ActionMenuItem.Title,
                    ActionMenuItem.Type,
                    ActionMenuItem.Size,
                    ActionMenuItem.Author.takeIf { section != SectionType.DEVICE_DOCUMENTS },
                    ActionMenuItem.Date
                ).map { it.get(asc, sortBy) }
            )
        } else if (section == SectionType.CLOUD_TRASH) {
            // trash action block
            add(ActionMenuItem.Restore)
            add(ActionMenuItem.Delete)
        } else {
            // action block
            if (section != SectionType.DEVICE_DOCUMENTS) add(ActionMenuItem.Download)
            add(ActionMenuItem.Move)
            add(ActionMenuItem.Copy)
            add(ActionMenuItem.Delete)
        }
    }

    private fun getSelectItems(selected: Boolean, allSelected: Boolean, divider: Boolean = true) =
        mutableListOf<ActionMenuItem>().apply {
            if (!selected) {
                add(ActionMenuItem.Select)
            } else {
                add(ActionMenuItem.Deselect)
            }
            if (!allSelected) add(ActionMenuItem.SelectAll)
            if (divider) add(ActionMenuItem.Divider)
        }

    private fun getRoomRootItems(
        section: Int,
        selected: Boolean,
        allSelected: Boolean,
        asc: Boolean,
        sortBy: String?,
    ) = mutableListOf<ActionMenuItem>().apply {
        // select block
        addAll(getSelectItems(selected, allSelected, !selected || section == SectionType.CLOUD_ARCHIVE_ROOM))
        // sort block
        if (!selected) {
            addAll(
                arrayOf(
                    ActionMenuItem.Title,
                    ActionMenuItem.RoomType,
                    ActionMenuItem.RoomTags,
                    ActionMenuItem.Author,
                    ActionMenuItem.Date
                ).map { it.get(asc, sortBy) }
            )
        } else if (section == SectionType.CLOUD_ARCHIVE_ROOM) {
            // archive action block
            add(ActionMenuItem.Restore)
            add(ActionMenuItem.Delete)
        }
    }

    private fun getRoomFolderItems(
        selected: Boolean,
        empty: Boolean,
        allSelected: Boolean,
        asc: Boolean,
        sortBy: String?,
        currentRoom: Boolean,
        security: Security,
    ) = mutableListOf<ActionMenuItem>().apply {
        val showCopyLink = !currentRoom && !selected
        if (!selected) {
            add(
                ActionMenuItem.ManageRoom.get(
                    listOfNotNull(
                        ActionMenuItem.Info,
                        ActionMenuItem.EditRoom.takeIf { security.editRoom },
                        ActionMenuItem.Invite.takeIf { security.editRoom },
                        ActionMenuItem.CopyLink(true),
                        ActionMenuItem.Divider,
                        ActionMenuItem.Archive.takeIf { security.editRoom },
                        ActionMenuItem.Download,
                        ActionMenuItem.LeaveRoom
                    )
                )
            )
        }
        if (!empty) {
            if (!selected) {
                add(
                    ActionMenuItem.SortBy.get(
                        listOf(
                            ActionMenuItem.Title,
                            ActionMenuItem.Type,
                            ActionMenuItem.Size,
                            ActionMenuItem.Author,
                            ActionMenuItem.Date
                        ).map { it.get(asc, sortBy) }
                    )
                )
                add(ActionMenuItem.Divider)
            }
            addAll(getSelectItems(selected, allSelected, false))
            if (selected) {
                add(ActionMenuItem.Divider)
                add(ActionMenuItem.Download)
                add(ActionMenuItem.Move)
                add(ActionMenuItem.Copy)
                add(ActionMenuItem.Delete)
            }
        }
        if (showCopyLink) {
            add(ActionMenuItem.Divider)
            add(ActionMenuItem.CopyLink(false))
        }
    }
}