package lib.toolkit.base.ui.dialogs.common

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import lib.toolkit.base.R
import lib.toolkit.base.ui.dialogs.base.BaseDialog
import lib.toolkit.base.ui.dialogs.common.holders.*

class CommonDialog : BaseDialog() {

    companion object {

        val TAG: String = CommonDialog::class.java.simpleName

        private const val TAG_TYPE = "TAG_TYPE"

        @JvmStatic
        fun newInstance(): CommonDialog {
            return CommonDialog()
        }
    }

    interface OnCommonDialogClose {
        fun onCommonClose()
    }

    interface OnClickListener {
        fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?)
        fun onCancelClick(dialogs: Dialogs?, tag: String?)
        fun onCloseCommonDialog() {

        }
    }

    interface ViewHolder {
        fun init()
        fun show()
        fun hide()
        fun setClickListener(listener: OnClickListener?)
        fun save(state: Bundle)
        fun restore(state: Bundle)
        fun getType(): Dialogs
        fun getTag(): String?
        fun getValue(): String? = null
        var isBackPress: Boolean
    }

    enum class Dialogs {
        NONE, WAITING, PROGRESS, EDIT_LINE, EDIT_MULTILINE, QUESTION, INFO
    }

    private val viewHolders = hashMapOf<Dialogs, ViewHolder>(
        Dialogs.WAITING to WaitingHolder(
            this
        ),
        Dialogs.PROGRESS to ProgressHolder(
            this
        ),
        Dialogs.EDIT_LINE to EditLineHolder(
            this
        ),
        Dialogs.EDIT_MULTILINE to EditMultilineHolder(
            this
        ),
        Dialogs.QUESTION to QuestionHolder(
            this
        ),
        Dialogs.INFO to InfoHolder(
            this
        )
    )

    private var frManager: FragmentManager? = null
    private var onClickListener: OnClickListener? = null
    private var dialogType: Dialogs = Dialogs.NONE

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClickListener) {
            onClickListener = context
        } else {
            throw ClassCastException(activity.toString() + " must implement OnClickListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewHolders[dialogType]?.save(outState)
        outState.putSerializable(TAG_TYPE, dialogType)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_common, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDialogAdded() {
        super.onDialogAdded()
        initViews()
    }

    override fun onCancel(dialog: DialogInterface) {
        closeHandler.postDelayed({
            onClickListener?.onCancelClick(dialogType, viewHolders[dialogType]?.getTag())
        }, 100)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClickListener?.onCloseCommonDialog()
    }

    override fun onBackPressed(): Boolean {
        if (viewHolders[dialogType]?.isBackPress != false) {
            super.onBackPressed()
        }

        onClickListener?.onCancelClick(dialogType, viewHolders[dialogType]?.getTag())
        return false
    }

    private fun init(savedInstanceState: Bundle?) {
        restoreValues(savedInstanceState)
        baseActivity.setCommonDialogOpen()
        initViews()
    }

    private fun restoreValues(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            dialogType = bundle.getSerializable(TAG_TYPE) as Dialogs
            viewHolders[dialogType]?.restore(savedInstanceState)
            savedInstanceState.clear()
        }
    }

    private fun initViews() {
        view?.let {
            actionHolder { dialog, holder -> if (dialog == dialogType) holder.init() }
            actionHolder { dialog, holder -> if (dialog != dialogType) holder.hide() }
            viewHolders[dialogType]?.setClickListener(onClickListener)
            viewHolders[dialogType]?.show()
        }
    }

    private fun actionHolder(action: (Dialogs, ViewHolder) -> Unit) {
        for ((key, value) in viewHolders) {
            action.invoke(key, value)
        }
    }

    fun show(type: Dialogs) {
        frManager?.let {
            dialogType = type
            show(it, TAG)
        }
    }

    fun setFragmentManager(fragmentManager: FragmentManager): CommonDialog {
        frManager = fragmentManager
        return this
    }

    fun waiting(): WaitingHolder.Builder {
        return (viewHolders[Dialogs.WAITING] as WaitingHolder).Builder()
    }

    fun editLine(): EditLineHolder.Builder {
        return (viewHolders[Dialogs.EDIT_LINE] as EditLineHolder).Builder()
    }

    fun editMultiline(): EditMultilineHolder.Builder {
        return (viewHolders[Dialogs.EDIT_MULTILINE] as EditMultilineHolder).Builder()
    }

    fun question(): QuestionHolder.Builder {
        return (viewHolders[Dialogs.QUESTION] as QuestionHolder).Builder()
    }

    fun progress(): ProgressHolder.Builder {
        return (viewHolders[Dialogs.PROGRESS] as ProgressHolder).Builder()
    }

    fun info(): InfoHolder.Builder {
        return (viewHolders[Dialogs.INFO] as InfoHolder).Builder()
    }

}







