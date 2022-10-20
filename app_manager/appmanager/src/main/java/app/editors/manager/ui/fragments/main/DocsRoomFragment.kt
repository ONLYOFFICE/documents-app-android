package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.forEach
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.ui.dialogs.AddRoomBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.popup.MainActionBarPopup
import app.editors.manager.ui.popup.SelectActionBarPopup
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.popup.ActionBarPopupItem

class DocsRoomFragment : DocsCloudFragment() {

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean) {
        if (cloudPresenter.isCurrentRoom && cloudPresenter.isRoot) {
            AddRoomBottomDialog().apply {
                onClickListener = object : AddRoomBottomDialog.OnClickListener {
                    override fun onActionButtonClick(roomType: Int) {
                        UiUtils.showEditDialog(
                            context = requireContext(),
                            title = getString(R.string.dialog_create_room),
                            value = getString(R.string.dialog_create_room_value),
                            acceptListener = { title ->
                                cloudPresenter.createRoom(title, roomType)
                            },
                            requireValue = true
                        )
                    }

                    override fun onActionDialogClose() {
                        this@DocsRoomFragment.onActionDialogClose()
                    }

                }
            }.show(parentFragmentManager, AddRoomBottomDialog.TAG)
        } else {
            super.onActionDialog(isThirdParty, isDocs)
        }
    }

    override fun showSelectedActionBarMenu(excluded: List<ActionBarPopupItem>) {
        return super.showSelectedActionBarMenu(
            excluded = listOf(
                SelectActionBarPopup.Move,
                SelectActionBarPopup.Copy,
                SelectActionBarPopup.Download
            )
        )
    }

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {
        super.showMainActionBarMenu(excluded = MainActionBarPopup.sortPopupItems)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_selection_archive -> cloudPresenter.archiveSelectedRooms()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStateMenuSelection() {
        menuInflater?.inflate(R.menu.docs_select_room, menu)
        menu?.forEach { menuItem ->
            menuItem.isVisible = true
            UiUtils.setMenuItemTint(requireContext(), menuItem, lib.toolkit.base.R.color.colorPrimary)
        }
        setAccountEnable(false)
    }

    override fun onContextButtonClick(buttons: ContextBottomDialog.Buttons?) {
        when (buttons) {
            ContextBottomDialog.Buttons.ARCHIVE -> {
                cloudPresenter.archiveRoom()
            }
            ContextBottomDialog.Buttons.PIN -> {
                cloudPresenter.pinRoom()
            }
            else -> super.onContextButtonClick(buttons)
        }
    }

    override fun getFilters(): Boolean {
        val filter = presenter.preferenceTool.filter
        return filter.roomType != RoomFilterType.None || filter.author.id.isNotEmpty()
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