package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import app.editors.manager.databinding.ListExplorerActionMenuBinding
import app.editors.manager.managers.utils.isVisible
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class ActionBottomDialog : BaseBottomDialog() {

    enum class Buttons {
        NONE, SHEET, PRESENTATION, DOC, FOLDER, PHOTO, UPLOAD, STORAGE
    }

    interface OnClickListener {
        fun onActionButtonClick(buttons: Buttons?)
        fun onActionDialogClose()
    }

    private var viewBinding: ListExplorerActionMenuBinding? = null
    private var onClickListener: OnClickListener? = null
    private var isThirdParty = false
    private var isDocs = true
    private var isLocal = false
    private var isWebDav = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        restoreValues(savedInstanceState)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_THIRD_PARTY, isThirdParty)
        outState.putBoolean(TAG_DOCS, isDocs)
        outState.putBoolean(TAG_LOCAL, isLocal)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        init(dialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        onClickListener = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClickListener?.onActionDialogClose()
    }

    private fun initListeners() {
        viewBinding?.let {
            it.listExplorerActionDocs.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.DOC)
            }
            it.listExplorerActionFolder.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.FOLDER)
            }
            it.listExplorerActionPhoto.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.PHOTO)
            }
            it.listExplorerActionUpload.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.UPLOAD)
            }
            it.listExplorerActionStorage.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.STORAGE)
            }
            it.listExplorerActionSheet.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.SHEET)
            }
            it.listExplorerActionPresentation.setOnClickListener {
                onClickListener?.onActionButtonClick(Buttons.PRESENTATION)
            }
            dismiss()
        }
    }

    private fun restoreValues(savedInstanceState: Bundle?) {
        savedInstanceState?.let { state ->
            isThirdParty = state.getBoolean(TAG_THIRD_PARTY)
            isDocs = state.getBoolean(TAG_DOCS)
            isLocal = state.getBoolean(TAG_LOCAL)
        }
    }

    private fun init(dialog: Dialog) {
        viewBinding = ListExplorerActionMenuBinding.inflate(layoutInflater).also {
            dialog.setContentView(it.root)
            dialog.setCanceledOnTouchOutside(true)
            setViewState()
            initListeners()
        }
    }

    private fun setViewState() {
        viewBinding?.let {
            it.viewLineSeparatorStorage.viewLineSeparator.isVisible = isThirdParty
            it.listExplorerActionStorage.isVisible = isThirdParty

            //TODO check it
            it.listExplorerActionDocs.isVisible = !isDocs || isLocal
            it.listExplorerActionPresentation.isVisible = !isDocs || isLocal
            it.listExplorerActionSheet.isVisible = !isDocs || isLocal
            it.listExplorerActionUpload.isVisible = isLocal && isWebDav
        }
    }

    companion object {

        @JvmField
        val TAG = ActionBottomDialog::class.java.simpleName
        private const val TAG_THIRD_PARTY = "TAG_THIRD_PARTY"
        private const val TAG_DOCS = "TAG_DOCS"
        private const val TAG_LOCAL = "TAG_LOCAL"

        @JvmStatic
        fun newInstance(): ActionBottomDialog {
            return ActionBottomDialog()
        }
    }
}
