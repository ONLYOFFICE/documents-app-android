package app.editors.manager.ui.dialogs.explorer

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import lib.toolkit.base.managers.utils.TimeUtils

sealed class ExplorerContextItem(
    val icon: Int,
    val title: Int,
) : ExplorerContextBlockOrder, ExplorerContextItemVisible {

    fun get(state: ExplorerContextState): ExplorerContextItem? = takeIf { state.visible(this) }

    class Header(val state: ExplorerContextState) : ExplorerContextItem(
        icon = -1,
        title = -1
    ), ExplorerContextBlockOrder.Header {
        var info = state.headerInfo ?: TimeUtils.formatDate(state.item.updated)
    }

    class Edit(state: ExplorerContextState) : ExplorerContextItem(
        icon = getIcon(state.item),
        title = getTitle(state)
    ), ExplorerContextBlockOrder.Common {

        companion object {

            fun getIcon(item: Item) = when {
                (item as? CloudFile)?.isPdfForm == true -> R.drawable.ic_access_fill_form
                else -> R.drawable.ic_list_context_edit
            }

            fun getTitle(state: ExplorerContextState) = when {
                state.section.isRoom && state.isRoot -> R.string.list_context_edit_room
                (state.item as? CloudFile)?.isPdfForm == true -> R.string.list_context_fill
                else -> R.string.list_context_edit
            }
        }
    }

    object Share : ExplorerContextItem(
        icon = lib.toolkit.base.R.drawable.ic_list_context_share,
        title = R.string.list_context_share
    ), ExplorerContextBlockOrder.Common

    class ExternalLink(state: ExplorerContextState) : ExplorerContextItem(
        icon = R.drawable.ic_list_context_external_link,
        title = getTitle(state)
    ), ExplorerContextBlockOrder.Common {

        companion object {

            fun getTitle(state: ExplorerContextState) = when {
                state.section.isRoom && state.isRoot -> R.string.list_context_copy_general_link
                else -> R.string.list_context_get_external_link
            }
        }
    }

    object RoomInfo : ExplorerContextItem(
        icon = R.drawable.ic_drawer_menu_about,
        title = R.string.list_context_info
    ), ExplorerContextBlockOrder.Common

    object Reconnect : ExplorerContextItem(
        icon = R.drawable.ic_refresh,
        title = R.string.room_storage_reconnect
    ), ExplorerContextBlockOrder.Common

    object AddUsers : ExplorerContextItem(
        icon = R.drawable.ic_add_users,
        title = R.string.share_invite_user
    ), ExplorerContextBlockOrder.Common


    object Send : ExplorerContextItem(
        icon = R.drawable.ic_list_context_send_copy,
        title = lib.toolkit.base.R.string.export_send_copy
    ), ExplorerContextBlockOrder.Common

    class Pin(pinned: Boolean) : ExplorerContextItem(
        icon = if (!pinned) R.drawable.ic_pin_to_top else R.drawable.ic_unpin,
        title = if (!pinned) R.string.list_context_pin_to_top else R.string.list_context_unpin
    ), ExplorerContextBlockOrder.Common

    object Download : ExplorerContextItem(
        icon = R.drawable.ic_list_context_download,
        title = R.string.list_context_create_download
    ), ExplorerContextBlockOrder.Common

    class Favorites(val enabled: Boolean, val favorite: Boolean) : ExplorerContextItem(
        icon = getIcon(favorite),
        title = getTitle(favorite)
    ), ExplorerContextBlockOrder.Common {

        companion object {
            private fun getIcon(favorite: Boolean) = if (!favorite)
                R.drawable.ic_favorites_outline else
                R.drawable.ic_favorites_fill

            private fun getTitle(favorite: Boolean) = if (!favorite)
                R.string.list_context_add_to_favorite else
                R.string.list_context_delete_from_favorite
        }
    }

    object Location : ExplorerContextItem(
        icon = R.drawable.ic_list_context_location,
        title = R.string.list_context_open_location
    ), ExplorerContextBlockOrder.Common

    object Move : ExplorerContextItem(
        icon = R.drawable.ic_list_context_move,
        title = R.string.list_context_move
    ), ExplorerContextBlockOrder.Operation

    object Copy : ExplorerContextItem(
        icon = R.drawable.ic_list_context_copy,
        title = R.string.list_context_create_copy
    ), ExplorerContextBlockOrder.Operation

    object Duplicate : ExplorerContextItem(
        icon = R.drawable.ic_list_context_duplicate,
        title = R.string.list_context_duplicate
    ), ExplorerContextBlockOrder.Common

    class Notifications(val muted: Boolean) : ExplorerContextItem(
        icon = getIcon(muted),
        title = getTitle(muted)
    ), ExplorerContextBlockOrder.Common {

        companion object {

            fun getTitle(muted: Boolean) = if (muted) {
                R.string.list_context_notification_enable
            } else {
                R.string.list_context_notification_disable
            }

            fun getIcon(muted: Boolean) = if (muted) {
                R.drawable.ic_notification_on
            } else {
                R.drawable.ic_notification_off
            }
        }
    }

    object Upload : ExplorerContextItem(
        icon = R.drawable.ic_list_action_upload,
        title = R.string.list_context_upload_to_portal
    ), ExplorerContextBlockOrder.Common

    object Rename : ExplorerContextItem(
        icon = R.drawable.ic_list_context_rename,
        title = R.string.list_context_rename
    ), ExplorerContextBlockOrder.Common

    object CreateRoom : ExplorerContextItem(
        icon = R.drawable.ic_create_room,
        title = R.string.dialog_create_room
    ), ExplorerContextBlockOrder.Common

    object Archive : ExplorerContextItem(
        icon = R.drawable.ic_room_archive,
        title = R.string.context_room_move_to_archive
    ), ExplorerContextBlockOrder.Remove

    object Restore : ExplorerContextItem(
        icon = R.drawable.ic_trash_restore,
        title = R.string.device_trash_files_restore
    ), ExplorerContextBlockOrder.Remove

    object ShareDelete : ExplorerContextItem(
        icon = R.drawable.drawable_ic_visibility_off,
        title = R.string.list_context_remove_from_list
    ), ExplorerContextBlockOrder.Remove

    class Delete(state: ExplorerContextState) : ExplorerContextItem(
        icon = getIcon(state),
        title = getTitle(state)
    ), ExplorerContextBlockOrder.Remove {

        companion object {

            private fun getIcon(state: ExplorerContextState): Int = with(state) {
                when {
                    section.isRoom && !section.isArchive && isRoot -> R.drawable.ic_leave
                    else -> R.drawable.ic_list_context_delete
                }
            }

            private fun getTitle(state: ExplorerContextState): Int = with(state) {
                when {
                    isStorageFolder && !section.isRoom -> R.string.list_context_delete_storage
                    section == ApiContract.Section.Recent -> R.string.list_context_delete_recent
                    section.isRoom && !section.isArchive && isRoot -> R.string.list_context_leave_room
                    else -> R.string.list_context_delete
                }
            }
        }
    }

}