package app.editors.manager.ui.fragments.main

import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import app.editors.manager.R
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.ui.dialogs.AddRoomBottomDialog
import app.editors.manager.ui.dialogs.ContextBottomDialog
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.popup.ActionBarPopupItem

class DocsRoomFragment : DocsCloudFragment() {

    override fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean) {
        if (cloudPresenter.isCurrentRoom) {
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

    }

    override fun showMainActionBarMenu(excluded: List<ActionBarPopupItem>) {

    }

    override fun setMenuFilterEnabled(isEnabled: Boolean) {
        filterItem?.isVisible = false
        filterItem?.isEnabled = false
        presenter.preferenceTool.filter = Filter()
        onStateUpdateFilterMenu()
    }

    override fun onStateUpdateFilterMenu() {
        filterItem?.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_toolbar_filter_disable)
    }

    override fun onStateMenuSelection() {
        menuInflater?.inflate(R.menu.docs_select_room, menu)
        menu?.findItem(R.id.toolbar_selection_archive)?.setVisible(true)?.also {
            UiUtils.setMenuItemTint(requireContext(), it, lib.toolkit.base.R.color.colorPrimary)
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