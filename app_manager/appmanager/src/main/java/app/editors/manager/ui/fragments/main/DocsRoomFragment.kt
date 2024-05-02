package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.setFragmentResultListener
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.dialogs.AddRoomBottomDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.popup.MainPopup
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.setFragmentResultListener
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class DocsRoomFragment : DocsCloudFragment() {
    private val isRoom get() = cloudPresenter.isCurrentRoom && cloudPresenter.isRoot

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(KEY_ROOM_CREATED_REQUEST) { _, bundle ->
            val roomId = bundle.getString(KEY_RESULT_ROOM_ID)
            if (!roomId.isNullOrEmpty()) {
                presenter.openFolder(roomId, 0)
            }
        }
    }

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean) {
        if (isRoom) {
            setFragmentResultListener { bundle ->
                onActionDialogClose()
                if (bundle?.getInt("type") != -1) {
                    showAddRoomFragment(bundle?.getInt("type") ?: 2)
                }
            }
            AddRoomBottomDialog().show(parentFragmentManager, AddRoomBottomDialog.TAG)
        } else {
            super.onActionDialog(isThirdParty, isDocs)
        }
    }

    override fun showMainActionPopup(vararg excluded: MainPopupItem) {
        MainPopup(
            context = requireContext(),
            section = if (isRoom) presenter.getSectionType() else -1,
            clickListener = mainActionBarClickListener,
            sortBy = presenter.preferenceTool.sortBy.orEmpty(),
            isAsc = isAsc,
            excluded = excluded.toList()
        ).show(requireActivity().window.decorView)
    }

    override fun showSelectActionPopup(vararg excluded: SelectPopupItem) {
        return if (isRoom) {
            super.showSelectActionPopup(
                SelectPopupItem.Operation.Move,
                SelectPopupItem.Operation.Copy,
                SelectPopupItem.Download
            )
        } else super.showSelectActionPopup(*excluded)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_selection_archive -> cloudPresenter.archiveSelectedRooms()
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
            ExplorerContextItem.Reconnect -> reconnectStorage()
            ExplorerContextItem.Archive -> cloudPresenter.archiveRoom()
            ExplorerContextItem.AddUsers -> ShareActivity.show(this, cloudPresenter.itemClicked, false)
            is ExplorerContextItem.Edit -> cloudPresenter.editRoom()
            is ExplorerContextItem.ExternalLink -> cloudPresenter.copyGeneralLink()
            is ExplorerContextItem.Pin -> cloudPresenter.pinRoom()
            is ExplorerContextItem.Delete -> cloudPresenter.checkRoomOwner()
            else -> super.onContextButtonClick(contextItem)
        }
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        when (tag) {
            "leave" -> {
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

    override fun onPlaceholder(type: PlaceholderViews.Type) {
        val isRoom = (presenter.currentFolder?.roomType ?: -1) > -1
        if (type == PlaceholderViews.Type.EMPTY && isRoom) {
            super.onPlaceholder(PlaceholderViews.Type.EMPTY_ROOM)
        } else {
            super.onPlaceholder(type)
        }
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

    }

    companion object {

        val TAG = DocsRoomFragment::class.java.simpleName
        const val KEY_RESULT_ROOM_ID = "key_result_room_id"

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