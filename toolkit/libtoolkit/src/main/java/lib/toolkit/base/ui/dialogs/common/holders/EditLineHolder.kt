package lib.toolkit.base.ui.dialogs.common.holders

import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.views.edits.BaseInputFilter

class EditLineHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private const val TAG_EDIT_VALUE = "TAG_EDIT_VALUE"
        private const val TAG_EDIT_HINT_VALUE = "TAG_EDIT_HINT_VALUE"
        private const val TAG_HINT_VALUE = "TAG_HINT_VALUE"
        private const val TAG_ERROR_VALUE = "TAG_ERROR_VALUE"
        private const val TAG_COLOR_TINT = "TAG_COLOR_TINT"
    }

    private lateinit var mLayout: ConstraintLayout
    private lateinit var mEditInputLayout: TextInputLayout
    private lateinit var mEditValueView: AppCompatEditText
    private lateinit var mEditHintView: AppCompatEditText

    private var mEditValue: String? = null
    private var mEditHintValue: String? = null
    private var mHintValue: String? = null
    private var mErrorValue: String? = null
    private var mIsPassword: Boolean = false
    private var mColorTint: Int = android.R.color.black

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
            mLayout = findViewById(R.id.dialogCommonEditLineLayout)
            mEditInputLayout = findViewById(R.id.dialogCommonEditLineTextInputLayout)
            mEditHintView = findViewById(R.id.dialogCommonEditLineHintEdit)
            mEditValueView = findViewById<AppCompatEditText>(R.id.dialogCommonEditLineValueEdit).apply {
                filters = arrayOf<InputFilter>(EditFilter())
            }
        }
    }

    override fun show() {
        super.show()
        mLayout.visibility = View.VISIBLE
        dialog.view?.post {
            if (!mEditValue.isNullOrEmpty()) {
                mEditValueView.setText(mEditValue)
                if (!mEditValueView.text.isNullOrEmpty()) {
                    mEditValueView.setSelection(0, mEditValue!!.length)
                }
                mAcceptView.isEnabled = mEditValue!!.trim { it <= ' ' }.isNotEmpty()
            } else {
                mEditValueView.setText("")
            }

            if (!mEditHintValue.isNullOrEmpty()) {
                mEditValueView.hint = mEditHintValue
            }

            if (mHintValue != null) {
                mEditHintView.visibility = View.VISIBLE
                mEditHintView.setText(mHintValue)
            } else {
                mEditHintView.visibility = View.GONE
            }

            if (mIsPassword) {
                mEditValueView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                mEditValueView.inputType = InputType.TYPE_CLASS_TEXT
            }

            if (!mErrorValue.isNullOrBlank()) {
                mEditInputLayout.isErrorEnabled = true
                mEditInputLayout.error = mErrorValue
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mEditValueView.background.colorFilter =
                    BlendModeColorFilter(ContextCompat.getColor(dialog.context!!, mColorTint), BlendMode.SRC_ATOP)
            } else {
                mEditValueView.background.colorFilter =
                    PorterDuffColorFilter(ContextCompat.getColor(dialog.context!!, mColorTint), PorterDuff.Mode.SRC_ATOP)
            }
            mEditValueView.postDelayed({
                KeyboardUtils.showKeyboard(mEditValueView)
            }, 100)
        }
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
            bundle.putString(TAG_HINT_VALUE, mHintValue)
            bundle.putString(TAG_ERROR_VALUE, mErrorValue)
            bundle.putInt(TAG_COLOR_TINT, mColorTint)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let { bundle ->
            mEditValue = bundle.getString(TAG_EDIT_VALUE)
            mEditHintValue = bundle.getString(TAG_EDIT_HINT_VALUE)
            mHintValue = bundle.getString(TAG_HINT_VALUE)
            mErrorValue = bundle.getString(TAG_ERROR_VALUE)
            mColorTint = bundle.getInt(TAG_COLOR_TINT)
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.EDIT_LINE

    override fun getValue(): String? = mEditValueView.text.toString()

    private inner class EditFilter : BaseInputFilter() {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            super.filter(source, start, end, dest, dstart, dend)
            // Hide hint
            mAcceptView.isEnabled = mResultString.trim { it <= ' ' }.isNotBlank()

            // Check for allowed symbols
            val checkedString = StringUtils.getAllowedString(mResultString)
            return if (checkedString != null) {
                mErrorValue = dialog.getString(R.string.dialogs_edit_forbidden_symbols)
                mEditInputLayout.error = mErrorValue + StringUtils.DIALOG_FORBIDDEN_SYMBOLS
                mAcceptView.isEnabled = mResultString.length > 1
                ""
            } else {
                mErrorValue = null
                mEditInputLayout.isErrorEnabled = false
                null
            }
        }
    }

    inner class Builder {

        fun setTag(value: String?): Builder {
            mTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            mTopTitle = value
            return this
        }

        fun setBottomTitle(value: String?): Builder {
            mBottomTitle = value
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

        fun setHintValue(value: String?): Builder {
            mHintValue = value
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

        fun setError(value: String?): Builder {
            mErrorValue = value
            return this
        }

        fun setIsPassword(password: Boolean): Builder {
            mIsPassword = password
            return this
        }

        fun setColorTint(int: Int): Builder {
            mColorTint = int
            return this
        }

        fun show() {
            dialog.show(CommonDialog.Dialogs.EDIT_LINE)
        }

    }

}