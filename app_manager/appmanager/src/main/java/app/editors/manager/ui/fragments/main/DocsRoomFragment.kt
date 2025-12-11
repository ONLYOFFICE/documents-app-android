package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.setFragmentResultListener
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.managers.tools.ActionMenuItem
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.mvp.models.list.Templates
import app.editors.manager.ui.dialogs.ActionBottomDialog
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.fragments.room.order.RoomOrderDialogFragment
import app.editors.manager.ui.fragments.share.InviteUsersFragment
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class DocsRoomFragment : DocsCloudFragment() {

    private val isRoom get() = presenter.isCurrentRoom && presenter.isRoot

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
        if (presenter.roomClicked?.roomType == ApiContract.RoomType.FILL_FORMS_ROOM){
            when (buttons) {
                ActionBottomDialog.Buttons.UPLOAD -> presenter.showFileChooserFragment()
                ActionBottomDialog.Buttons.IMPORT -> presenter.uploadPermission(".pdf")
                else -> super.onActionButtonClick(buttons)
            }
        } else {
            super.onActionButtonClick(buttons)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_selection_archive -> presenter.archiveRooms(true)
            R.id.toolbar_selection_delete -> if (presenter.isTemplatesFolder) {
                presenter.onDeleteTemplates()
                return true
            }
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

    override fun showDeleteDialog(count: Int, toTrash: Boolean, tag: String) {
        if (presenter.isTemplatesFolder || tag == TAG_DELETE_TEMPLATE) {
            showQuestionDialog(
                title = resources.getQuantityString(
                    R.plurals.dialogs_question_delete_template_title,
                    count
                ),
                string = resources.getQuantityString(
                    R.plurals.dialogs_question_message_template,
                    count,
                    presenter.currentSelectedFolderTitle
                ),
                acceptButton = getString(R.string.dialogs_question_accept_delete),
                cancelButton = getString(R.string.dialogs_common_cancel_button),
                tag = tag,
                acceptErrorTint = true
            )
        } else {
            super.showDeleteDialog(count, toTrash, tag)
        }
    }

    override fun onContextButtonClick(contextItem: ExplorerContextItem) {
        when (contextItem) {
            ExplorerContextItem.Duplicate -> presenter.duplicateRoom()
            ExplorerContextItem.RoomInfo -> showRoomInfoFragment()
            ExplorerContextItem.Reconnect -> reconnectStorage()
            ExplorerContextItem.Archive -> presenter.archiveRooms(true)
            ExplorerContextItem.AddUsers -> showInviteUsersDialog()
            ExplorerContextItem.EditIndex -> showEditIndexDialog()
            is ExplorerContextItem.Notifications -> presenter.muteRoomNotifications(!contextItem.muted)
            is ExplorerContextItem.ExternalLink -> presenter.copyLinkFromContextMenu()
            is ExplorerContextItem.Pin -> presenter.pinRoom()
            is ExplorerContextItem.Delete -> {
                when {
                    presenter.isTemplatesFolder -> showDeleteDialog(tag = TAG_DELETE_TEMPLATE)
                    presenter.isRoot -> presenter.checkRoomOwner()
                    else -> super.onContextButtonClick(contextItem)
                }
            }
            is ExplorerContextItem.Lock -> presenter.lockFile()
            is ExplorerContextItem.SaveAsTemplate -> presenter.createTemplate()
            is ExplorerContextItem.AccessSettings -> presenter.editTemplateAccessSettings()
            else -> super.onContextButtonClick(contextItem)
        }
    }

    override val actionMenuClickListener: (ActionMenuItem) -> Unit = { item ->
        when (item) {
            ActionMenuItem.Archive -> {
                presenter.popToRoot()
                presenter.archiveRooms(true)
            }
            ActionMenuItem.Info -> showRoomInfoFragment()
            ActionMenuItem.EditRoom -> presenter.editRoom()
            ActionMenuItem.Invite -> showInviteUsersDialog()
            ActionMenuItem.LeaveRoom -> presenter.checkRoomOwner()
            ActionMenuItem.EditIndex -> showEditIndexDialog()
            ActionMenuItem.ExportIndex -> presenter.exportIndex()
            ActionMenuItem.Download -> presenter.createDownloadFile()
            ActionMenuItem.DeleteTemplate -> showDeleteDialog(tag = TAG_DELETE_TEMPLATE)
            ActionMenuItem.EditTemplate -> presenter.editTemplate()
            ActionMenuItem.AccessSettings -> presenter.editTemplateAccessSettings()
            else -> super.actionMenuClickListener.invoke(item)
        }
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        when (tag) {
            TAG_LEAVE_ROOM -> presenter.leaveRoom()
            TAG_DELETE_TEMPLATE -> presenter.deleteTemplate()
            TAG_PROTECTED_ROOM_OPEN_FOLDER,
            TAG_PROTECTED_ROOM_SHOW_INFO,
            TAG_PROTECTED_ROOM_DOWNLOAD -> presenter.authRoomViaLink(value.orEmpty(), tag)
            else -> super.onAcceptClick(dialogs, value, tag)
        }
    }

    override fun getFilters(): Boolean {
        return if (isRoom || presenter.isTemplatesFolder) {
            val filter = presenter.preferenceTool.filter
            filter.roomType != RoomFilterType.None ||
                    filter.author.id.isNotEmpty() ||
                    filter.tags.isNotEmpty() ||
                    filter.provider != null
        } else super.getFilters()
    }

    override fun onRoomViaLinkPasswordRequired(error: Boolean, tag: String) {
        (requireActivity() as? BaseActivity)?.showEditDialog(
            title = getString(R.string.rooms_protected_room),
            value = "",
            editHint = getString(R.string.login_enterprise_password_hint),
            acceptTitle = getString(lib.toolkit.base.R.string.common_ok),
            cancelTitle = getString(lib.toolkit.base.R.string.common_cancel),
            isPassword = true,
            error = getString(R.string.rooms_invalid_password).takeIf { error },
            tag = tag,
            bottomTitle = null
        )
    }

    override fun onDocsGet(list: List<Entity>?) {
        super.onDocsGet(prepareDocsList(list))
    }

    override fun onDocsRefresh(list: List<Entity>?) {
        super.onDocsRefresh(prepareDocsList(list))
        setMenuFilterEnabled(true)
    }

    override fun onDocsNext(list: List<Entity>?) {
        super.onDocsNext(prepareDocsList(list))
    }

    private fun prepareDocsList(list: List<Entity>?): List<Entity> {
        val newList = list.orEmpty().toMutableList()
        if (ApiContract.SectionType.isRoom(presenter.getSectionType()) && presenter.isRoot
            && (!presenter.isRegularUser || presenter.currentFolder?.security?.create == true)
        ) {
            newList.add(0, Templates)
        }
        return newList
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

    private fun showEditIndexDialog() {
        presenter.currentFolder?.id?.let { id ->
            RoomOrderDialogFragment.show(
                activity = requireActivity(),
                folderId = id,
                onSuccess = presenter::refresh
            )
        }
    }

    companion object {

        val TAG: String = DocsRoomFragment::class.java.simpleName
        const val KEY_RESULT_ROOM_ID = "key_result_room_id"
        const val KEY_RESULT_ROOM_TYPE = "key_result_room_type"
        const val TAG_LEAVE_ROOM = "tag_leave_room"
        const val TAG_DELETE_TEMPLATE = "tag_delete_template"
        const val TAG_PROTECTED_ROOM_OPEN_FOLDER = "tag_protected_room_open"
        const val TAG_PROTECTED_ROOM_SHOW_INFO = "tag_protected_room_info"
        const val TAG_PROTECTED_ROOM_DOWNLOAD = "tag_protected_room_download"

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