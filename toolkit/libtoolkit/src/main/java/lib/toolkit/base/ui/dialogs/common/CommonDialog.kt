package lib.toolkit.base.ui.dialogs.common

import android.app.Dialog
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

        @JvmField
        val TAG = CommonDialog::class.java.simpleName

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
        fun isBackPress(): Boolean
    }

    enum class Dialogs {
        NONE, WAITING, PROGRESS, EDIT_LINE, EDIT_MULTILINE, QUESTION, INFO
    }

    private val mViewHolders = hashMapOf<Dialogs, ViewHolder>(
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

    private var mFragmentManager: FragmentManager? = null
    private var mOnClickListener: OnClickListener? = null
    private var mDialogType: Dialogs = Dialogs.NONE


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mViewHolders[mDialogType]?.save(outState)
        outState.putSerializable(TAG_TYPE, mDialogType)
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
        mCloseHandler.postDelayed({
            mOnClickListener?.onCancelClick(mDialogType, mViewHolders[mDialogType]?.getTag())
        }, 100)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mOnClickListener?.onCloseCommonDialog()
    }

    override fun onBackPressed(): Boolean {
        if (mViewHolders[mDialogType]?.isBackPress() != false) {
            super.onBackPressed()
        }

        mOnClickListener?.onCancelClick(mDialogType, mViewHolders[mDialogType]?.getTag())
        return false
    }

    private fun init(savedInstanceState: Bundle?) {
        restoreValues(savedInstanceState)
        mBaseActivity.setCommonDialogOpen()
        initViews()
    }

    private fun restoreValues(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            mDialogType = bundle.getSerializable(TAG_TYPE) as Dialogs
            mViewHolders[mDialogType]?.restore(savedInstanceState)
            savedInstanceState.clear()
        }
    }

    private fun initViews() {
        view?.post {
            actionHolder { dialog, holder -> holder.init() }
            actionHolder { dialog, holder -> if (dialog != mDialogType) holder.hide() }
            mViewHolders[mDialogType]?.setClickListener(mOnClickListener)
            mViewHolders[mDialogType]?.show()
        }
    }

    private fun actionHolder(action: (Dialogs, ViewHolder) -> Unit) {
        for ((key, value) in mViewHolders) {
            action.invoke(key, value)
        }
    }

    fun show(type: Dialogs) {
        mFragmentManager?.let {
            mDialogType = type
            show(it, TAG)
        }
    }

    fun setFragmentManager(fragmentManager: FragmentManager): CommonDialog {
        mFragmentManager = fragmentManager
        return this
    }

    fun setOnClickListener(listener: OnClickListener): CommonDialog {
        mOnClickListener = listener
        return this
    }

    fun waiting(): WaitingHolder.Builder {
        return (mViewHolders[Dialogs.WAITING] as WaitingHolder).Builder()
    }

    fun editLine(): EditLineHolder.Builder {
        return (mViewHolders[Dialogs.EDIT_LINE] as EditLineHolder).Builder()
    }

    fun editMultiline(): EditMultilineHolder.Builder {
        return (mViewHolders[Dialogs.EDIT_MULTILINE] as EditMultilineHolder).Builder()
    }

    fun question(): QuestionHolder.Builder {
        return (mViewHolders[Dialogs.QUESTION] as QuestionHolder).Builder()
    }

    fun progress(): ProgressHolder.Builder {
        return (mViewHolders[Dialogs.PROGRESS] as ProgressHolder).Builder()
    }

    fun info(): InfoHolder.Builder {
        return (mViewHolders[Dialogs.INFO] as InfoHolder).Builder()
    }

}







