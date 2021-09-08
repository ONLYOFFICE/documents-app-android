package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.ListExplorerContextMenuBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.isVisible
import com.google.android.material.snackbar.Snackbar
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog
import java.io.Serializable

class ContextBottomDialog : BaseBottomDialog() {

    enum class Buttons {
        NONE, FOLDER, EDIT, SHARE, EXTERNAL, MOVE, COPY, DOWNLOAD, 
        RENAME, DELETE, SHARE_DELETE, FAVORITE_ADD, FAVORITE_DELETE
    }

    interface OnClickListener {
        fun onContextButtonClick(buttons: Buttons?)
        fun onContextDialogClose()
    }

    private val preferenceTool: PreferenceTool = App.getApp().appComponent.preference
    private val networkSettings: NetworkSettings = App.getApp().appComponent.networkSettings
    private var viewBinding: ListExplorerContextMenuBinding? = null

    var onClickListener: OnClickListener? = null

    var state = State()
        set(value) {
            field = value
            setItemSharedState(state.isShared)
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        restoreValues(savedInstanceState)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(TAG_STATE, state)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        init(dialog)
        super.setupDialog(dialog, style)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClickListener?.onContextDialogClose()
    }

    private fun restoreValues(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            state = it.getSerializable(TAG_STATE) as State
        }
    }

    private fun init(dialog: Dialog) {
        viewBinding = ListExplorerContextMenuBinding.inflate(LayoutInflater.from(context))
        viewBinding?.let {
            dialog.setContentView(it.root)
            it.listExplorerContextHeaderTitleText.text = state.title
            it.listExplorerContextHeaderInfoText.text = state.info
            it.listExplorerContextHeaderImage.setImageResource(state.iconResId)
            UiUtils.setImageTint(it.listExplorerContextHeaderImage, R.color.colorOnSurface)
            if (!state.isFolder) {
                app.editors.manager.managers.utils.UiUtils.setFileIcon(
                    it.listExplorerContextHeaderImage, StringUtils.getExtensionFromPath(state.title))
            }
        }
        setViewState()
        initListeners()
    }

    private fun setViewState() {
        viewBinding?.let {
            if (state.isRecent) {
                setRecentState()
                return
            }
            if (state.isTrash) {
                setTrashState()
                return
            }

            /** Common */
            it.listExplorerContextCopy.isVisible = true
            if (state.isWebDav) {
                setWebDav()
                return
            }
            if (state.isLocal) {
                setLocalState()
                return
            }

            /** Folders or Files */
            if (state.isFolder) {
                /** Folder is storage */
                if (state.isStorage) {
                    it.listExplorerContextDeleteText.text =
                        getString(R.string.list_context_delete_storage)
                } else {
                    it.listExplorerContextDeleteText.text =
                        getString(R.string.list_context_delete)
                }
                it.listExplorerContextDownload.isVisible = !state.isOneDrive
                it.listExplorerContextExternalLink.isVisible = state.isOneDrive
            } else {
                /** File can downloaded */
                it.listExplorerContextDownload.isVisible = true
                if (StringUtils.convertServerVersion(networkSettings.serverVersion)
                    >= 11 && preferenceTool.isFavoritesEnabled) {
                    it.viewLineSeparatorFavorites.viewLineSeparator.isVisible = true
                    if (state.isFavorite) {
                        it.listExplorerContextDeleteFromFavorite.isVisible = true
                    } else {
                        it.listExplorerContextAddToFavorite.isVisible = true
                    }
                }

                /** File is document */
                if (state.isDocs && !state.isPdf) {
                    it.viewLineSeparatorEdit.viewLineSeparator.isVisible = state.isItemEditable
                    it.listExplorerContextEdit.isVisible = state.isItemEditable
                }

                /** File can access by link */
                it.listExplorerContextExternalLink.isVisible = state.isCanShare
            }

            /**Folders and files*/
            /**Context is editable*/
            it.listExplorerContextMove.isVisible = state.isContextEditable
            setDeleteVisibility(state.isContextEditable)

            /** Item can edit */
            it.listExplorerContextRename.isVisible = state.isItemEditable

            /** Item can share */
            it.viewLineSeparatorShare.viewLineSeparator.isVisible = state.isCanShare && !state.isOneDrive
            it.listExplorerContextShare.isVisible = state.isCanShare && !state.isOneDrive

            /** Only for share section, instead of delete */
            it.listExplorerContextShareDelete.isVisible = state.isDeleteShare

            if (preferenceTool.isPersonalPortal && !state.isFolder) {
                it.viewLineSeparatorShare.viewLineSeparator.isVisible = true
                it.listExplorerContextExternalLink.isVisible = true
            }
        }
    }

    private fun setTrashState() {
        viewBinding?.let {
            it.listExplorerContextMove.isVisible = true
            it.listExplorerContextDelete.isVisible = true
        }
    }

    private fun setRecentState() {
        val info = if (state.isLocal) {
            getString(R.string.this_device) + getString(R.string.placeholder_point) + state.info
        } else {
            state.info
        }
        viewBinding?.let {
            it.listExplorerContextHeaderInfoText.text = info
            it.listExplorerContextCopy.isVisible = false
            it.listExplorerContextDownload.isVisible = false
            it.listExplorerContextDeleteText.text = getString(R.string.list_context_delete_recent)
            it.listExplorerContextDelete.isVisible = true
        }
    }

    private fun setWebDav() {
        viewBinding?.let {
            it.listExplorerContextDelete.isVisible = true
            it.listExplorerContextMove.isVisible = true
            it.listExplorerContextRename.isVisible = true
            it.listExplorerContextDownload.isVisible = state.isFolder
            it.viewLineSeparatorDelete.viewLineSeparator.isVisible = true
        }
    }

    private fun setUploadToPortal(isVisible: Boolean) {
        viewBinding?.let {
            it.contextDownloadImage.setImageResource(R.drawable.ic_list_action_upload)
            it.contextDownloadText.text = getString(R.string.list_context_upload_to_portal)
            it.listExplorerContextDownload.isVisible = isVisible
        }
    }

    private fun setLocalState() {
        viewBinding?.let {
            setUploadToPortal(!state.isFolder)
            it.listExplorerContextMove.isVisible = true
            it.listExplorerContextCopy.isVisible = true
            it.listExplorerContextDelete.isVisible = true
            it.listExplorerContextRename.isVisible = true
            setDeleteVisibility(true)
        }
    }

    private fun setDeleteVisibility(isVisible: Boolean) {
        viewBinding?.let {
            it.viewLineSeparatorDelete.viewLineSeparator.isVisible = isVisible
            it.listExplorerContextDelete.isVisible = isVisible
        }
    }

    fun setItemSharedState(isShared: Boolean) {
        state.isShared = isShared
    }

    fun setItemSharedEnable(isEnable: Boolean) {
        viewBinding?.listExplorerContextExternalLink?.isEnabled = isEnable
    }

    fun showMessage(message: String) {
        val snackBar: Snackbar = UiUtils.getShortSnackBar(viewBinding?.root!!)
        if (state.isShared) {
            showSendLinkButton(snackBar)
        }
        snackBar.setText(message).show()
    }

    private fun showSendLinkButton(snackBar: Snackbar) {
        snackBar.setAction(R.string.operation_snackbar_send_link) {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_TEXT, KeyboardUtils.getTextFromClipboard(App.getApp()))
            }, getString(R.string.operation_snackbar_send_link)))
        }
    }

    private fun initListeners() {
        viewBinding?.let {
            it.listExplorerContextFolderName.setOnClickListener(Buttons.FOLDER)
            it.listExplorerContextEdit.setOnClickListener(Buttons.EDIT)
            it.listExplorerContextShare.setOnClickListener(Buttons.SHARE)
            it.listExplorerContextExternalLink.setOnClickListener(Buttons.EXTERNAL)
            it.listExplorerContextMove.setOnClickListener(Buttons.MOVE)
            it.listExplorerContextCopy.setOnClickListener(Buttons.COPY)
            it.listExplorerContextDownload.setOnClickListener(Buttons.DOWNLOAD)
            it.listExplorerContextRename.setOnClickListener(Buttons.RENAME)
            it.listExplorerContextDelete.setOnClickListener(Buttons.DELETE)
            it.listExplorerContextShareDelete.setOnClickListener(Buttons.SHARE_DELETE)
            it.listExplorerContextAddToFavorite.setOnClickListener(Buttons.FAVORITE_ADD)
            it.listExplorerContextDeleteFromFavorite.setOnClickListener(Buttons.FAVORITE_DELETE)
        }
    }

    private fun View.setOnClickListener(button: Buttons) {
        this.setOnClickListener {
            onClickListener?.onContextButtonClick(button)
            dismiss()
        }
    }

    companion object {
        @JvmField
        val TAG = ContextBottomDialog::class.java.simpleName
        private const val TAG_STATE = "TAG_STATE"

        @JvmStatic
        fun newInstance(): ContextBottomDialog {
            return ContextBottomDialog()
        }
    }

    data class State (
        var title: String = "",
        var info: String = "",
        var iconResId: Int = 0,
        var isFolder: Boolean = false,
        var isShared: Boolean = false,
        var isCanShare: Boolean = false,
        var isDocs: Boolean = false,
        var isStorage: Boolean = false,
        var isItemEditable: Boolean = false,
        var isContextEditable: Boolean = false,
        var isDeleteShare: Boolean = false,
        var isPdf: Boolean = false,
        var isLocal: Boolean = false,
        var isRecent: Boolean = false,
        var isWebDav: Boolean = false,
        var isTrash: Boolean = false,
        var isFavorite: Boolean = false,
        var isOneDrive: Boolean = false
    ) : Serializable
}

