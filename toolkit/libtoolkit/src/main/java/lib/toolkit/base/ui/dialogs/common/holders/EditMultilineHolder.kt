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
            mAcceptView.isEnabled = mResultString.isNotEmpty() && !mResultString.trim { it <= ' ' }.isEmpty()
            return null
        }
    }

    private lateinit var mLayout: ConstraintLayout
    private lateinit var mEditInputLayout: TextInputLayout
    private lateinit var mEditValueView: AppCompatEditText

    private var mEditValue: String? = null
    private var mEditHintValue: String? = null
    private var mCursorPosition: Int? = null

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialogCommonAcceptButton -> {
                KeyboardUtils.hideKeyboard(mEditValueView)
                mOnClickListener?.onAcceptClick(getType(), getValue(), mTag)
            }

            R.id.dialogCommonCancelButton -> {
                KeyboardUtils.hideKeyboard(mEditValueView)
                mOnClickListener?.onCancelClick(getType(), mTag)
            }
        }
    }

    override fun init() {
        super.init()
        dialog.view?.apply {
            mLayout = findViewById(R.id.dialogCommonEditMultilineLayout)
            mEditInputLayout = findViewById(R.id.dialogCommonEditMultilineTextInputLayout)
            mEditValueView = findViewById<AppCompatEditText>(R.id.dialogCommonEditMultilineValueEdit).apply {
                filters = arrayOf<InputFilter>(EditFilter())
            }
        }
    }

    override fun show() {
        super.show()
        mLayout.visibility = View.VISIBLE
        mEditValueView.setText(mEditValue)
        mEditValueView.hint = mEditHintValue
        mEditValueView.setSelection(mCursorPosition ?: 0)
        mEditValueView.post { KeyboardUtils.showKeyboard(mEditValueView) }
    }

    override fun hide() {
        super.hide()
        mLayout.visibility = View.GONE
        KeyboardUtils.hideKeyboard(mEditValueView)
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let { bundle ->
            bundle.putString(TAG_EDIT_VALUE, mEditValueView.text.toString())
            bundle.putString(TAG_EDIT_HINT_VALUE, mEditHintValue)
            bundle.putInt(TAG_CURSOR_POSITION, mEditValueView.selectionEnd)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let {
            mEditValue = it.getString(TAG_EDIT_VALUE)
            mEditHintValue = it.getString(TAG_EDIT_HINT_VALUE)
            mCursorPosition = state.getInt(TAG_CURSOR_POSITION, 0)
        }
    }

    override fun getValue(): String? {
        return mEditValueView.text.toString()
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.EDIT_MULTILINE


    inner class Builder {

        fun setTag(value: String?): Builder {
            mTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            mTopTitle = value
            return this
        }

        fun setEditValue(value: String?): Builder {
            mEditValue = value
            return this
        }

        fun setEditHintValue(value: String?): Builder {
            mEditHintValue = value
            return this
        }

        fun setAcceptTitle(value: String?): Builder {
            mAcceptTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            mCancelTitle = value
            return this
        }

        fun show() {
            dialog.show(getType())
        }
    }

}