package lib.toolkit.base.ui.dialogs.common.holders

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.DIALOG_FORBIDDEN_SYMBOLS
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.views.edits.BaseInputFilter


class EditLineHolder(private val dialog: CommonDialog) : BaseHolder(dialog) {

    companion object {
        private const val TAG_EDIT_VALUE = "TAG_EDIT_VALUE"
        private const val TAG_HINT_VALUE = "TAG_HINT_VALUE"
        private const val TAG_ERROR_VALUE = "TAG_ERROR_VALUE"
    }

    private var layout: FrameLayout? = null
    private var editInputLayout: TextInputLayout? = null
    private var editValueView: EditText? = null

    private var editValue: String? = null
    private var hintValue: String? = null
    private var errorValue: String? = null
    private var suffixValue: String? = null
    private var isPassword: Boolean = false
    private var forbiddenSymbols: String = DIALOG_FORBIDDEN_SYMBOLS

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
            layout = findViewById(R.id.dialogCommonEditLineLayout)
            editValueView = findViewById(R.id.dialogCommonEditLineValueEdit)
            editInputLayout = findViewById(R.id.dialogCommonEditLineTextInputLayout)
        }
    }

    override fun setTint() {
        super.setTint()
        dialog.view?.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                editValueView?.textCursorDrawable?.setTint(colorPrimary)
            }
            editInputLayout?.boxStrokeColor = colorPrimary
            editInputLayout?.hintTextColor = ColorStateList.valueOf(colorPrimary)
        }
    }

    override fun show() {
        super.show()
        layout?.visibility = View.VISIBLE
        acceptView.isEnabled = editValue?.isNotEmpty() == true
        dialog.view?.post {
            editValueView?.apply {
                if (isPassword) {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    doOnTextChanged { text, _, _, _ ->
                        if (text.isNullOrEmpty()) {
                            editInputLayout?.error = null
                            editInputLayout?.setEndIconTintList(editInputLayout?.defaultHintTextColor)
                        }
                        acceptView.isEnabled = !text?.trim().isNullOrEmpty()
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

            editInputLayout?.apply {
                suffixText = suffixValue
                hintValue?.let(::setHint)
                if (!errorValue.isNullOrEmpty()) {
                    isErrorEnabled = true
                    error = errorValue
                    if (isPassword) {
                        errorIconDrawable = null
                        setEndIconTintList(ColorStateList.valueOf(context.getColor(R.color.colorError)))
                    }
                }

                if (isPassword) {
                    endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                mEditValueView.background.colorFilter =
//                    BlendModeColorFilter(ContextCompat.getColor(dialog.context!!, mColorTint), BlendMode.SRC_ATOP)
//            } else {
//                mEditValueView.background.colorFilter =
//                    PorterDuffColorFilter(ContextCompat.getColor(dialog.context!!, mColorTint), PorterDuff.Mode.SRC_ATOP)
//            }

            if (errorValue.isNullOrEmpty()) {
                editValueView?.postDelayed({
                    KeyboardUtils.showKeyboard(editValueView)
                }, 100)
            } else {
                editValueView?.clearFocus()
            }
        }
    }

    override fun hide() {
        super.hide()
//        editValueView?.addTextChangedListener(null)
        layout?.visibility = View.GONE
        KeyboardUtils.hideKeyboard(editValueView)
    }

    override fun save(state: Bundle) {
        super.save(state)
        state.let { bundle ->
            bundle.putString(TAG_EDIT_VALUE, editValueView?.text.toString())
            bundle.putString(TAG_HINT_VALUE, hintValue)
            bundle.putString(TAG_ERROR_VALUE, errorValue)
        }
    }

    override fun restore(state: Bundle) {
        super.restore(state)
        state.let { bundle ->
            editValue = bundle.getString(TAG_EDIT_VALUE)
            hintValue = bundle.getString(TAG_HINT_VALUE)
            errorValue = bundle.getString(TAG_ERROR_VALUE)
        }
    }

    override fun getType(): CommonDialog.Dialogs = CommonDialog.Dialogs.EDIT_LINE

    override fun getValue(): String = editValueView?.text.toString()

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
            acceptView.isEnabled = mResultString.trim { it <= ' ' }.isNotBlank()

            // Check for allowed symbols
            val checkedString = StringUtils.getAllowedString(mResultString, forbiddenSymbols)
            return if (checkedString != null) {
                errorValue = dialog.getString(R.string.dialogs_edit_forbidden_symbols)
                editInputLayout?.error = errorValue + forbiddenSymbols
                acceptView.isEnabled = mResultString.length > 1
                ""
            } else if (StringUtils.getAllowedName(mResultString)) {
                errorValue = dialog.getString(R.string.dialogs_edit_forbidden_name)
                editInputLayout?.error = errorValue
                acceptView.isEnabled = false
                null
            } else {
                errorValue = null
                editInputLayout?.isErrorEnabled = false
                null
            }
        }
    }

    inner class Builder {

        fun setTag(value: String?): Builder {
            holderTag = value
            return this
        }

        fun setTopTitle(value: String?): Builder {
            topTitle = value
            return this
        }

        fun setBottomTitle(value: String?): Builder {
            bottomTitle = value
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
            acceptTitle = value
            return this
        }

        fun setCancelTitle(value: String?): Builder {
            cancelTitle = value
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

        fun setSuffix(value: String?): Builder {
            suffixValue = value
            return this
        }

        fun setForbiddenSymbols(value: String): Builder {
            forbiddenSymbols = value
            return this
        }

        fun show(fragmentManager: FragmentManager) {
            dialog.show(fragmentManager, CommonDialog.Dialogs.EDIT_LINE)
        }

    }

}