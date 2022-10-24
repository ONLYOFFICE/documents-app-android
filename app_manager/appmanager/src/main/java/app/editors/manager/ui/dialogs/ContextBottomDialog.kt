package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.ListExplorerContextMenuBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFileIcon
import app.editors.manager.mvp.models.explorer.Item
import com.google.android.material.snackbar.Snackbar
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog
import java.io.Serializable

class ContextBottomDialog : BaseBottomDialog() {

    enum class Buttons {
        NONE, FOLDER, EDIT, SHARE, EXTERNAL, MOVE, COPY, DOWNLOAD,
        RENAME, DELETE, SHARE_DELETE, FAVORITE, RESTORE, OPEN_LOCATION, ARCHIVE, PIN, INFO
    }

    interface OnClickListener {
        fun onContextButtonClick(buttons: Buttons?)
        fun onContextDialogClose()
    }

    private var viewBinding: ListExplorerContextMenuBinding? = null

    private val viewModel by viewModels<ContextBottomViewModel> {
        ContextBottomViewModelFactory(checkNotNull(arguments?.getSerializable(ARG_ITEM)) as Item, checkNotNull(arguments?.getInt(ARG_SECTION)))
    }

    var onClickListener: OnClickListener? = null

    var state = State()
        set(value) {
            field = value
            setItemSharedState(state.isShared)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.ContextMenuDialog)
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
        super.setupDialog(dialog, style)
        init(dialog)
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
            it.listExplorerContextHeaderImage.alpha = 1f
            if (!state.isFolder) {
                it.listExplorerContextHeaderImage.setFileIcon(StringUtils.getExtensionFromPath(state.title))
            }
        }
        setViewState()
//        lifecycleScope.launch {
//            viewModel.state.collect { items ->
//                items.forEach { contextItem ->
//                    when (contextItem) {
//                        ContextItems.Archive -> {
//                            viewBinding?.listExplorerContextArchive?.isVisible = true
//                        }
//                        ContextItems.Copy -> {
//                            viewBinding?.listExplorerContextCopy?.isVisible = true
//                        }
//                        ContextItems.Move -> {
//                            viewBinding?.listExplorerContextMove?.isVisible = true
//                        }
//                        is ContextItems.Delete -> {
////                            viewBinding?.viewLineSeparatorDelete?.root?.isVisible = true
//                            viewBinding?.listExplorerContextDelete?.isVisible = true
//                            contextItem.title?.let {
//                                viewBinding?.listExplorerContextDeleteText?.setText(contextItem.title)
//                            }
//                        }
//                        ContextItems.Disconnect -> {
//
//                        }
//                        ContextItems.Download -> {
//                            viewBinding?.listExplorerContextDownload?.isVisible = true
//                        }
//                        is ContextItems.Favorites -> {
//                            viewBinding?.listExplorerContextFavorite?.isVisible = true
//                            if (contextItem.isFavorite) {
//                                viewBinding?.favoriteImage?.setImageResource(R.drawable.ic_favorites_fill)
//                                viewBinding?.favoriteText?.setText(R.string.list_context_delete_from_favorite)
//                            } else {
//                                viewBinding?.favoriteText?.setText(R.string.list_context_add_to_favorite)
//                            }
//                        }
//                        is ContextItems.Header -> {
//                            viewBinding?.listExplorerContextHeaderTitleText?.text = contextItem.title
//                            viewBinding?.listExplorerContextHeaderInfoText?.text = contextItem.info
//                            viewBinding?.listExplorerContextHeaderImage?.setImageResource(contextItem.icon)
//                        }
//                        ContextItems.InternalShare -> {
//                            viewBinding?.listExplorerContextExternalLink?.isVisible = true
//                        }
//                        is ContextItems.Pin -> {
//                            if (contextItem.isPinned) {
//                                viewBinding?.listExplorerContextPin?.isVisible = true
//                                viewBinding?.listExplorerContextPinText?.setText(R.string.list_context_unpin)
//                            } else {
//                                viewBinding?.listExplorerContextPin?.isVisible = true
//                            }
//                        }
//                        ContextItems.Rename -> {
//                            viewBinding?.listExplorerContextRename?.isVisible = true
//                        }
//                        ContextItems.Restore -> {
//                            viewBinding?.listExplorerContextRestore?.isVisible = true
//                        }
//                        ContextItems.Share -> {
//                            viewBinding?.listExplorerContextShare?.isVisible = true
//                        }
//                        ContextItems.Upload -> {
//                            viewBinding?.listExplorerContextDownload?.isVisible = true
//                            viewBinding?.contextDownloadText?.setText(R.string.upload_to_portal)
//                            viewBinding?.contextDownloadImage?.setImageResource(R.drawable.ic_list_action_upload)
//                        }
//                        else -> {}
//                    }
//                }
//            }
//        }
        initListeners()
    }

    private fun setViewState() {
        viewBinding?.let { binding ->
            if (state.isRoom) {
                setRoomState()
                return
            }
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
                binding.listExplorerContextExternalLink.isVisible = state.isOneDrive || state.isGoogleDrive
                binding.listExplorerContextCopy.isVisible = !state.isGoogleDrive
            } else {
                /** File can downloaded */
                binding.listExplorerContextDownload.isVisible = true

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
            binding.listExplorerContextRename.isVisible = state.isCanRename

            /** Item can share */
            binding.viewLineSeparatorShare.root.isVisible = state.isCanShare && !state.isOneDrive && !state.isDropBox && !state.isGoogleDrive
            binding.listExplorerContextShare.isVisible = state.isCanShare && !state.isOneDrive && !state.isDropBox && !state.isGoogleDrive

            /** Only for share section, instead of delete */
            binding.listExplorerContextShareDelete.isVisible = state.isDeleteShare


            if (state.isPersonalAccount) {
                binding.listExplorerContextShare.isVisible = !state.isFolder
                binding.viewLineSeparatorShare.root.isVisible = !state.isFolder
                binding.listExplorerContextExternalLink.isVisible = false
            }
        }
    }

    private fun setRoomState() {
        viewBinding?.let { binding ->
            if (!state.isTrash) {
                binding.listExplorerContextArchive.isVisible = true
                binding.listExplorerContextRename.isVisible = true
                binding.listExplorerContextPin.isVisible = true
                binding.listExplorerContextAddUser.isVisible = true
                binding.listExplorerContextDelete.isVisible = true
                binding.listExplorerContextInfo.isVisible = true
                binding.viewLineSeparatorDelete.root.isVisible = true
                if (state.isPin) binding.listExplorerContextPinText.setText(R.string.list_context_unpin)
            } else {
                binding.listExplorerContextRestoreText.setText(R.string.context_room_unarchive)
                setTrashState()
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
            setUploadToPortal(
                !state.isFolder && !(state.isGoogleDrive || state.isDropBox
                        || state.isOneDrive || state.isVisitor)
            )
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
            it.listExplorerContextAddUser.setOnClickListener(Buttons.SHARE)
            it.listExplorerContextExternalLink.setOnClickListener(Buttons.EXTERNAL)
            it.listExplorerContextMove.setOnClickListener(Buttons.MOVE)
            it.listExplorerContextCopy.setOnClickListener(Buttons.COPY)
            it.listExplorerContextDownload.setOnClickListener(Buttons.DOWNLOAD)
            it.listExplorerContextRename.setOnClickListener(Buttons.RENAME)
            it.listExplorerContextDelete.setOnClickListener(Buttons.DELETE)
            it.listExplorerContextShareDelete.setOnClickListener(Buttons.SHARE_DELETE)
            it.listExplorerContextInfo.setOnClickListener(Buttons.INFO)
            it.listExplorerContextFavorite.setOnClickListener(Buttons.FAVORITE)
            it.listExplorerContextRestore.setOnClickListener(Buttons.RESTORE)
            it.listExplorerContextArchive.setOnClickListener(Buttons.ARCHIVE)
            it.listExplorerContextPin.setOnClickListener(Buttons.PIN)
        }
    }

    private fun View.setOnClickListener(button: Buttons) {
        this.setOnClickListener {
            onClickListener?.onContextButtonClick(button)
            dismiss()
        }
    }

    companion object {
        val TAG: String = ContextBottomDialog::class.java.simpleName

        private const val TAG_STATE = "TAG_STATE"
        private const val ARG_ITEM = "item"
        private const val ARG_SECTION = "section"


        @JvmStatic
        fun newInstance(): ContextBottomDialog {
            return ContextBottomDialog()
        }

        fun newInstance(item: Item, section: Int): ContextBottomDialog {
            val dialog = ContextBottomDialog()
            dialog.arguments = Bundle().apply {
                putSerializable(ARG_ITEM, item)
                putInt(ARG_SECTION, section)
            }
            return dialog
        }
    }

    data class State(
        var title: String = "",
        var info: String = "",
        var iconResId: Int = 0,
        var isFolder: Boolean = false,
        var isShared: Boolean = false,
        var isCanShare: Boolean = false,
        var isCanRename: Boolean = false,
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
        var isPersonalAccount: Boolean = false,
        var isGoogleDrive: Boolean = false,
        var isVisitor: Boolean = false,
        var isRoom: Boolean = false,
        var isPin: Boolean = false
    ) : Serializable
}

