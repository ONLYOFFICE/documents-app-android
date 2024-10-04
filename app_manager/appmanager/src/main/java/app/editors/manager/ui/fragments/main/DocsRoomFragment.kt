package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.setFragmentResultListener
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.managers.tools.ActionMenuItem
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.fragments.share.InviteUsersFragment
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class DocsRoomFragment : DocsCloudFragment() {

    private val isRoom get() = cloudPresenter.isCurrentRoom && cloudPresenter.isRoot

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(KEY_ROOM_CREATED_REQUEST) { _, bundle ->
            val roomId = bundle.getString(KEY_RESULT_ROOM_ID)
            val roomType = bundle.getInt(KEY_RESULT_ROOM_TYPE)
            if (!roomId.isNullOrEmpty()) {
                presenter.openFolder(roomId, 0, roomType)
            }
        }
    }

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean, roomType: Int?) {
        if (isRoom) {
            showAddRoomBottomDialog(false)
        } else {
            super.onActionDialog(isThirdParty, isDocs, roomType)
        }
    }

    override fun onActionButtonClick(buttons: ActionBottomDialog.Buttons?) {
        when (buttons) {
            ActionBottomDialog.Buttons.UPLOAD -> presenter.showFileChooserFragment()
            ActionBottomDialog.Buttons.IMPORT -> presenter.uploadPermission(".pdf")
            else -> super.onActionButtonClick(buttons)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_selection_archive -> cloudPresenter.archiveRooms(true)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStateMenuSelection() {
        if (isRoom) {
            menuInflater?.inflate(R.menu.docs_select_room, menu)
            menu?.forEach { menuItem ->
                menuItem.isVisible = true
                UiUtils.setMenuItemTint(requireContext(), menuItem, lib.toolkit.base.R.color.colorPrimary)
            }
            setAccountEnable(false)
        } else super.onStateMenuSelection()
    }

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            ExplorerContextItem.Duplicate -> cloudPresenter.duplicateRoom()
            ExplorerContextItem.RoomInfo -> showRoomInfoFragment()
            ExplorerContextItem.Reconnect -> reconnectStorage()
            ExplorerContextItem.Archive -> cloudPresenter.archiveRooms(true)
            ExplorerContextItem.AddUsers -> showInviteUsersDialog()
            is ExplorerContextItem.ExternalLink -> cloudPresenter.copyLinkFromContextMenu()
            is ExplorerContextItem.Pin -> cloudPresenter.pinRoom()
            is ExplorerContextItem.Delete -> if (presenter.isRoot) cloudPresenter.checkRoomOwner() else super.onContextButtonClick(contextItem)
            else -> super.onContextButtonClick(contextItem)
        }
    }

    override val actionMenuClickListener: (ActionMenuItem) -> Unit = { item ->
        when (item) {
            ActionMenuItem.Archive -> {
                cloudPresenter.popToRoot()
                cloudPresenter.archiveRooms(true)
            }
            ActionMenuItem.Info -> showRoomInfoFragment()
            ActionMenuItem.EditRoom -> cloudPresenter.editRoom()
            ActionMenuItem.Invite -> showInviteUsersDialog()
            is ActionMenuItem.CopyLink -> cloudPresenter.copyLinkFromActionMenu(item.isRoom)
            ActionMenuItem.LeaveRoom -> cloudPresenter.checkRoomOwner()
            else -> super.actionMenuClickListener.invoke(item)
        }
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        when (tag) {
            TAG_LEAVE_ROOM -> {
                cloudPresenter.leaveRoom()
            }
            else -> super.onAcceptClick(dialogs, value, tag)
        }
    }

    override fun getFilters(): Boolean {
        return if (isRoom) {
            val filter = presenter.preferenceTool.filter
            filter.roomType != RoomFilterType.None ||
                    filter.author.id.isNotEmpty() ||
                    filter.tags.isNotEmpty() ||
                    filter.provider != null
        } else super.getFilters()
    }

    private fun reconnectStorage() {
        val room = presenter.itemClicked as? CloudFolder
        showStorageActivity(
            isMySection = true,
            isRoom = true,
            title = room?.title,
            providerKey = room?.providerKey,
            providerId = room?.providerId ?: -1
        )
    }

    private fun showInviteUsersDialog() {
        presenter.roomClicked?.let { room ->
            InviteUsersFragment.newInstance(room.id, room.roomType).show(parentFragmentManager, null)
        }
    }

    companion object {

        val TAG: String = DocsRoomFragment::class.java.simpleName
        const val KEY_RESULT_ROOM_ID = "key_result_room_id"
        const val KEY_RESULT_ROOM_TYPE = "key_result_room_type"
        const val TAG_LEAVE_ROOM = "tag_leave_room"

        fun newInstance(section: Int, rootPath: String): DocsCloudFragment {
            return DocsRoomFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_PATH, rootPath)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }

}