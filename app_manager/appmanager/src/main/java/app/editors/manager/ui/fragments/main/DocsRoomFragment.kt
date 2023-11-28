package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.forEach
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.dialogs.AddRoomBottomDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.popup.MainPopup
import app.editors.manager.ui.popup.MainPopupItem
import app.editors.manager.ui.popup.SelectPopupItem
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.setFragmentResultListener

class DocsRoomFragment : DocsCloudFragment() {

    private val isRoom get() = cloudPresenter.isCurrentRoom && cloudPresenter.isRoot

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean) {
        if (isRoom) {
            setFragmentResultListener { bundle ->
                if (bundle?.getInt("type") != -1) {
                    showAddRoomFragment(bundle?.getInt("type") ?: 2)
                }
                onActionDialogClose()
            }
            AddRoomBottomDialog().show(parentFragmentManager, AddRoomBottomDialog.TAG)
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
            ExplorerContextItem.Archive -> cloudPresenter.archiveRoom()
            ExplorerContextItem.AddUsers -> ShareActivity.show(this, cloudPresenter.itemClicked, false)
            is ExplorerContextItem.Edit -> cloudPresenter.editRoom()
            is ExplorerContextItem.ExternalLink -> cloudPresenter.copyGeneralLink()
            is ExplorerContextItem.Pin -> cloudPresenter.pinRoom()
            else -> super.onContextButtonClick(contextItem)
        }
        contextBottomDialog?.dismiss()
    }

    override fun getFilters(): Boolean {
        return if (isRoom) {
            val filter = presenter.preferenceTool.filter
            filter.roomType != RoomFilterType.None || filter.author.id.isNotEmpty()
        } else super.getFilters()
    }


    companion object {

        fun newInstance(stringAccount: String, section: Int, rootPath: String): DocsCloudFragment {
            return DocsRoomFragment().apply {
                arguments = Bundle(3).apply {
                    putString(KEY_ACCOUNT, stringAccount)
                    putString(KEY_PATH, rootPath)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }

}