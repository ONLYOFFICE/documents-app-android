package lib.toolkit.base.ui.dialogs.common.holders

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputLayout
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.views.edits.BaseInputFilter

class EditMultilineHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private const val TAG_EDIT_VALUE = "TAG_EDIT_VALUE"
        private const val TAG_EDIT_HINT_VALUE = "TAG_EDIT_HINT_VALUE"
        private const val TAG_CURSOR_POSITION = "TAG_CURSOR_POSITION"
    }

    private inner class EditFilter : BaseInputFilter() {
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            super.filter(source, start, end, dest, dstart, dend)
            acceptView.isEnabled = mResultString.isNotEmpty() && !mResultString.trim { it <= ' ' }.isEmpty()
            return null
        }
    }

    private var layout: ConstraintLayout? = null
    private var editInputLayout: TextInputLayout? = null
    private var editValueView: AppCompatEditText? = null

    private var mEditValue: String? = null
    private var editHintValue: String? = null
    private var mCursorPosition: Int? = null

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialogCommonAcceptButton -> {
                KeyboardUtils.hideKeyboard(editValueView)
                onClickListener?.onAcceptClick(getType(), getValue(), holderTag)
            }

            R.id.dialogCommonCancelButton -> {
                KeyboardUtils.hideKeyboard(editValueView)
                onClickListener?.onCancelClick(getType(), holderTag)
            }
        }
    }

    override fun init() {
        super.init()
        dialog.view?.apply {
            layout = findViewById(R.id.dialogCommonEditMultilineLayout)
            editInputLayout = findViewById(R.id.dialogCommonEditMultilineTextInputLayout)
            editValueView = findViewById<AppCompatEditText>(R.id.dialogCommonEditMultilineValueEdit).apply {
                filters = arrayOf<InputFilter>(EditFilter())
            }
        }
    }

    override fun show() {
        super.show()
        layout?.visibility = View.VISIBLE
        editValueView?.setText(mEditValue)
        editValueView?.hint = editHintValue
        editValueView?.setSelection(mCursorPosition ?: 0)
        editValueView?.post { KeyboardUtils.showKeyboard(editValueView) }
    }

    override fun hide() {
        super.hide()
        layout?.visibility = View.GONE
        KeyboardUtils.hideKeyboard(editValueView)
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let { bundle ->
            bundle.putString(TAG_EDIT_VALUE, editValueView?.text.toString())
            bundle.putString(TAG_EDIT_HINT_VALUE, editHintValue)
            bundle.putInt(TAG_CURSOR_POSITION, editValueView?.selectionEnd ?: -1)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let {
            mEditValue = it.getString(TAG_EDIT_VALUE)
            editHintValue = it.getString(TAG_EDIT_HINT_VALUE)
            mCursorPosition = state.getInt(TAG_CURSOR_POSITION, 0)
        }
    }

    override fun getValue(): String? {
        return editValueView?.text.toString()
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.EDIT_MULTILINE


    inner class Builder {

        fun setTag(value: String?): Builder {
            holderTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            topTitle = value
            return this
        }

        fun setEditValue(value: String?): Builder {
            mEditValue = value
            return this
        }

        fun setEditHintValue(value: String?): Builder {
            editHintValue = value
            return this
        }

        fun setAcceptTitle(value: String?): Builder {
            acceptTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            cancelTitle = value
            return this
        }

        fun show() {
            dialog.show(getType())
        }
    }

}