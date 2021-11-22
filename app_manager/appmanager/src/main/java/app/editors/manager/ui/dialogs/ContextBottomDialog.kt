package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
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
import app.editors.manager.managers.utils.ManagerUiUtils
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
        RENAME, DELETE, SHARE_DELETE, FAVORITE_ADD, FAVORITE_DELETE, RESTORE
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
            UiUtils.setImageTint(it.listExplorerContextHeaderImage, lib.toolkit.base.R.color.colorOnSurface)
            if (!state.isFolder) {
                ManagerUiUtils.setFileIcon(
                    it.listExplorerContextHeaderImage, StringUtils.getExtensionFromPath(state.title))
            }
        }
        setViewState()
        initListeners()
    }

    private fun setViewState() {
        viewBinding?.let { binding ->
            if (state.isRecent) {
                setRecentState()
                return
            }
            if (state.isTrash) {
                setTrashState()
                return
            }

            /** Common */
            binding.listExplorerContextCopy.isVisible = true
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
                    binding.listExplorerContextDeleteText.text =
                        getString(R.string.list_context_delete_storage)
                } else {
                    binding.listExplorerContextDeleteText.text =
                        getString(R.string.list_context_delete)
                }
                binding.listExplorerContextDownload.isVisible = !state.isOneDrive
                binding.listExplorerContextExternalLink.isVisible = state.isOneDrive
            } else {
                /** File can downloaded */
                binding.listExplorerContextDownload.isVisible = true
                if (StringUtils.convertServerVersion(networkSettings.serverVersion)
                    >= 11 && preferenceTool.isFavoritesEnabled) {
                    binding.viewLineSeparatorFavorites.root.isVisible = true
                    if (state.isFavorite) {
                        binding.listExplorerContextDeleteFromFavorite.isVisible = true
                    } else {
                        binding.listExplorerContextAddToFavorite.isVisible = true
                    }
                }

                /** File is document */
                if (state.isDocs && !state.isPdf) {
                    binding.viewLineSeparatorEdit.root.isVisible = state.isItemEditable
                    binding.listExplorerContextEdit.isVisible = state.isItemEditable
                }

                /** File can access by link */
                binding.listExplorerContextExternalLink.isVisible = state.isCanShare
            }

            /**Folders and files*/
            /**Context is editable*/
            binding.listExplorerContextMove.isVisible = state.isContextEditable
            setDeleteVisibility(state.isContextEditable)

            /** Item can edit */
            binding.listExplorerContextRename.isVisible = state.isItemEditable

            /** Item can share */
            binding.viewLineSeparatorShare.root.isVisible = state.isCanShare && !state.isOneDrive && !state.isDropBox
            binding.listExplorerContextShare.isVisible = state.isCanShare && !state.isOneDrive && !state.isDropBox

            /** Only for share section, instead of delete */
            binding.listExplorerContextShareDelete.isVisible = state.isDeleteShare

            if (state.isPersonalAccount) {
                binding.listExplorerContextShare.isVisible = !state.isFolder
                binding.viewLineSeparatorShare.root.isVisible = !state.isFolder
                binding.listExplorerContextExternalLink.isVisible = false
            }
        }
    }

    private fun setTrashState() {
        viewBinding?.let {
            it.listExplorerContextRestore.isVisible = true
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
            it.viewLineSeparatorDelete.root.isVisible = true
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
            it.viewLineSeparatorDelete.root.isVisible = isVisible
            it.listExplorerContextDelete.isVisible = isVisible
        }
    }

    fun setItemSharedState(isShared: Boolean) {
        state.isShared = isShared
    }

    fun setItemSharedEnable(isEnable: Boolean) {
        viewBinding?.listExplorerContextExternalLink?.isEnabled = isEnable
    }

    fun showMessage(message: String, view: View) {
        val snackBar: Snackbar = UiUtils.getShortSnackBar(view)
        if (state.isShared) {
            showSendLinkButton(snackBar, view.context)
        }
        snackBar.setText(message).show()
    }

    private fun showSendLinkButton(snackBar: Snackbar, context: Context) {
        snackBar.setAction(R.string.operation_snackbar_send_link) {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_TEXT, KeyboardUtils.getTextFromClipboard(App.getApp()))
            }, context.getString(R.string.operation_snackbar_send_link)))
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
            it.listExplorerContextRestore.setOnClickListener(Buttons.RESTORE)
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
        val TAG: String = ContextBottomDialog::class.java.simpleName
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
        var isOneDrive: Boolean = false,
        var isDropBox: Boolean = false,
        var isPersonalAccount: Boolean = false
    ) : Serializable
}

