package lib.toolkit.base.ui.dialogs.common.holders

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.views.edits.BaseInputFilter

class EditLineHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private const val TAG_EDIT_VALUE = "TAG_EDIT_VALUE"
        private const val TAG_HINT_VALUE = "TAG_HINT_VALUE"
        private const val TAG_ERROR_VALUE = "TAG_ERROR_VALUE"
        private const val TAG_COLOR_TINT = "TAG_COLOR_TINT"
    }

    private lateinit var layout: FrameLayout
    private lateinit var editInputLayout: TextInputLayout
    private lateinit var editValueView: TextInputEditText

    private var editValue: String? = null
    private var hintValue: String? = null
    private var errorValue: String? = null
    private var isPassword: Boolean = false
    private var colorTint: Int = android.R.color.black

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialogCommonAcceptButton -> {
                KeyboardUtils.hideKeyboard(editValueView)
                mOnClickListener?.onAcceptClick(getType(), getValue(), mTag)
            }

            R.id.dialogCommonCancelButton -> {
                KeyboardUtils.hideKeyboard(editValueView)
                mOnClickListener?.onCancelClick(getType(), mTag)
            }
        }
    }

    override fun init() {
        super.init()
        dialog.view?.apply {
            layout = findViewById(R.id.dialogCommonEditLineLayout)
            editValueView = findViewById(R.id.dialogCommonEditLineValueEdit)
            editInputLayout = findViewById(R.id.dialogCommonEditLineTextInputLayout)
        }
    }

    override fun show() {
        super.show()
        layout.visibility = View.VISIBLE
        mAcceptView.isEnabled = false
        dialog.view?.post {
            editValueView.apply {
                if (isPassword) {
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    doOnTextChanged { _, _, _, _ ->
                        editInputLayout.error = null
                        mAcceptView.isEnabled = !text?.trim().isNullOrEmpty()
                    }
                } else {
                    if (!editValue.isNullOrEmpty()) {
                        setText(editValue)
                        if (!text.isNullOrEmpty()) {
                            setSelection(0, editValue?.length ?: 0)
                        }
                    }
                    filters = arrayOf<InputFilter>(EditFilter())
                }
            }

            editInputLayout.apply {
                hintValue?.let(::setHint)
                if (!errorValue.isNullOrEmpty()) {
                    isErrorEnabled = true
                    error = errorValue
                }

                if (isPassword && errorValue.isNullOrEmpty()) {
                    endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    endIconDrawable = AppCompatResources.getDrawable(context, R.drawable.drawable_selector_password_visibility)
                }
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                mEditValueView.background.colorFilter =
//                    BlendModeColorFilter(ContextCompat.getColor(dialog.context!!, mColorTint), BlendMode.SRC_ATOP)
//            } else {
//                mEditValueView.background.colorFilter =
//                    PorterDuffColorFilter(ContextCompat.getColor(dialog.context!!, mColorTint), PorterDuff.Mode.SRC_ATOP)
//            }

            editValueView.postDelayed({
                KeyboardUtils.showKeyboard(editValueView)
            }, 100)
        }
    }

    override fun hide() {
        super.hide()
        layout.visibility = View.GONE
        KeyboardUtils.hideKeyboard(editValueView)
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let { bundle ->
            bundle.putString(TAG_EDIT_VALUE, editValueView.text.toString())
            bundle.putString(TAG_HINT_VALUE, hintValue)
            bundle.putString(TAG_ERROR_VALUE, errorValue)
            bundle.putInt(TAG_COLOR_TINT, colorTint)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let { bundle ->
            editValue = bundle.getString(TAG_EDIT_VALUE)
            hintValue = bundle.getString(TAG_HINT_VALUE)
            errorValue = bundle.getString(TAG_ERROR_VALUE)
            colorTint = bundle.getInt(TAG_COLOR_TINT)
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.EDIT_LINE

    override fun getValue(): String = editValueView.text.toString()

    private inner class EditFilter : BaseInputFilter() {

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            super.filter(source, start, end, dest, dstart, dend)
            // Hide hint
            mAcceptView.isEnabled = mResultString.trim { it <= ' ' }.isNotBlank()

            // Check for allowed symbols
            val checkedString = StringUtils.getAllowedString(mResultString)
            return if (checkedString != null) {
                errorValue = dialog.getString(R.string.dialogs_edit_forbidden_symbols)
                editInputLayout.error = errorValue + StringUtils.DIALOG_FORBIDDEN_SYMBOLS
                mAcceptView.isEnabled = mResultString.length > 1
                ""
            } else if (StringUtils.getAllowedName(mResultString)) {
                errorValue = dialog.getString(R.string.dialogs_edit_forbidden_name)
                editInputLayout.error = errorValue
                mAcceptView.isEnabled = false
                null
            } else {
                errorValue = null
                editInputLayout.isErrorEnabled = false
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
            editValue = value
            return this
        }

        fun setEditHintValue(value: String?): Builder {
            hintValue = value
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
            errorValue = value
            return this
        }

        fun setIsPassword(password: Boolean): Builder {
            isPassword = password
            return this
        }

        fun setColorTint(int: Int): Builder {
            colorTint = int
            return this
        }

        fun show() {
            dialog.show(CommonDialog.Dialogs.EDIT_LINE)
        }

    }

}