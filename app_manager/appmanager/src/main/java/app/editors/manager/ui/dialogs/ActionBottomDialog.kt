package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
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
    var isThirdParty = false
    var isDocs = true
    var isLocal = false
    var isWebDav = false
    var onClickListener: OnClickListener? = null

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
            it.listExplorerActionDocs.setOnClickListener(Buttons.DOC)
            it.listExplorerActionFolder.setOnClickListener(Buttons.FOLDER)
            it.listExplorerActionPhoto.setOnClickListener(Buttons.PHOTO)
            it.listExplorerActionUpload.setOnClickListener(Buttons.UPLOAD)
            it.listExplorerActionStorage.setOnClickListener(Buttons.STORAGE)
            it.listExplorerActionSheet.setOnClickListener(Buttons.SHEET)
            it.listExplorerActionPresentation.setOnClickListener(Buttons.PRESENTATION)
        }
    }

    private fun View.setOnClickListener(button: Buttons) {
        this.setOnClickListener {
            onClickListener?.onActionButtonClick(button)
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
        viewBinding = ListExplorerActionMenuBinding.inflate(layoutInflater).apply {
            dialog.setContentView(root)
            dialog.setCanceledOnTouchOutside(true)
        }
        setViewState()
        initListeners()
    }

    private fun setViewState() {
        viewBinding?.let {
            it.viewLineSeparatorStorage.viewLineSeparator.isVisible = isThirdParty
            it.listExplorerActionStorage.isVisible = isThirdParty

            it.listExplorerActionDocs.isVisible = isDocs || isLocal
            it.listExplorerActionPresentation.isVisible = isDocs || isLocal
            it.listExplorerActionSheet.isVisible = isDocs || isLocal

            it.listExplorerActionUpload.isVisible = !isLocal || isWebDav
        }
    }

    companion object {

        val TAG: String = ActionBottomDialog::class.java.simpleName

        private const val TAG_THIRD_PARTY = "TAG_THIRD_PARTY"
        private const val TAG_DOCS = "TAG_DOCS"
        private const val TAG_LOCAL = "TAG_LOCAL"

        fun newInstance(): ActionBottomDialog {
            return ActionBottomDialog()
        }
    }
}
