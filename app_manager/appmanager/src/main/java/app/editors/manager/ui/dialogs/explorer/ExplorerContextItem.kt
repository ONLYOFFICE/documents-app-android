package app.editors.manager.ui.dialogs.explorer

import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import lib.toolkit.base.managers.utils.TimeUtils

sealed class ExplorerContextItem(
    val icon: Int,
    val title: Int,
) : ExplorerContextBlockOrder, ExplorerContextItemVisible {

    fun get(state: ExplorerContextState): ExplorerContextItem? = takeIf { state.visible(this) }

    class Header(state: ExplorerContextState) : ExplorerContextItem(
        icon = ManagerUiUtils.getIcon(state.item),
        title = -1
    ), ExplorerContextBlockOrder.Header {
        var fileName = state.item.title
        var info = state.headerInfo ?: TimeUtils.formatDate(state.item.updated)
    }

    object Edit : ExplorerContextItem(
        icon = R.drawable.ic_list_context_edit,
        title = R.string.list_context_edit
    ), ExplorerContextBlockOrder.Edit

    object Share : ExplorerContextItem(
        icon = R.drawable.ic_list_context_share,
        title = R.string.list_context_share
    ), ExplorerContextBlockOrder.Share

    object ExternalLink : ExplorerContextItem(
        icon = R.drawable.ic_list_context_external_link,
        title = R.string.list_context_get_external_link
    ), ExplorerContextBlockOrder.Share

    object RoomInfo : ExplorerContextItem(
        icon = R.drawable.ic_drawer_menu_about,
        title = R.string.list_context_info
    ), ExplorerContextBlockOrder.Share

    object AddUsers : ExplorerContextItem(
        icon = R.drawable.ic_add_users,
        title = R.string.list_context_add_users
    ), ExplorerContextBlockOrder.Share


    object Send : ExplorerContextItem(
        icon = R.drawable.ic_list_context_send_copy,
        title = R.string.list_context_send_copy
    ), ExplorerContextBlockOrder.Operation

    class Pin(pinned: Boolean) : ExplorerContextItem(
        icon = if (!pinned) R.drawable.ic_pin_to_top else R.drawable.ic_unpin,
        title = if (!pinned) R.string.list_context_pin_to_top else R.string.list_context_unpin
    ), ExplorerContextBlockOrder.Operation

    class Favorites(val enabled: Boolean, favorite: Boolean) : ExplorerContextItem(
        icon = getIcon(favorite),
        title = getTitle(favorite)
    ), ExplorerContextBlockOrder.Operation {

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
    ), ExplorerContextBlockOrder.Operation

    object Move : ExplorerContextItem(
        icon = R.drawable.ic_list_context_move,
        title = R.string.list_context_move
    ), ExplorerContextBlockOrder.Operation

    object Copy : ExplorerContextItem(
        icon = R.drawable.ic_list_context_copy,
        title = R.string.list_context_create_copy
    ), ExplorerContextBlockOrder.Operation

    object Download : ExplorerContextItem(
        icon = R.drawable.ic_list_context_download,
        title = R.string.list_context_create_download
    ), ExplorerContextBlockOrder.Operation

    object Upload : ExplorerContextItem(
        icon = R.drawable.ic_list_action_upload,
        title = R.string.list_context_upload_to_portal
    ), ExplorerContextBlockOrder.Operation

    object Rename : ExplorerContextItem(
        icon = R.drawable.ic_list_context_rename,
        title = R.string.list_context_rename
    ), ExplorerContextBlockOrder.Operation

    object Archive : ExplorerContextItem(
        icon = R.drawable.ic_room_archive,
        title = R.string.context_room_archive
    ), ExplorerContextBlockOrder.Remove

    class Restore(isRoom: Boolean) : ExplorerContextItem(
        icon = R.drawable.ic_trash_restore,
        title = if (!isRoom) R.string.device_trash_files_restore else R.string.context_room_unarchive
    ), ExplorerContextBlockOrder.Operation

    object ShareDelete : ExplorerContextItem(
        icon = R.drawable.drawable_ic_visibility_off,
        title = R.string.list_context_remove_from_list
    ), ExplorerContextBlockOrder.Remove

    class Delete(state: ExplorerContextState) : ExplorerContextItem(
        icon = R.drawable.ic_list_context_delete,
        title = getTitle(state)
    ), ExplorerContextBlockOrder.Remove {

        companion object {
            private fun getTitle(state: ExplorerContextState): Int =
                when {
                    state.isStorageFolder -> R.string.list_context_delete_storage
                    state.section == ApiContract.Section.Recent -> R.string.list_context_delete_recent
                    else -> R.string.list_context_delete
                }
        }
    }

}