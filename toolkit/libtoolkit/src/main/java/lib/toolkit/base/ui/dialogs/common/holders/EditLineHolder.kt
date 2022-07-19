package lib.toolkit.base.ui.dialogs.common.holders

import android.content.res.ColorStateList
import android.os.Build
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

    private var layout: FrameLayout? = null
    private var editInputLayout: TextInputLayout? = null
    private var editValueView: TextInputEditText? = null

    private var editValue: String? = null
    private var hintValue: String? = null
    private var errorValue: String? = null
    private var colorTint: Int? = null
    private var isPassword: Boolean = false

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editValueView?.textCursorDrawable?.setTint(colorPrimary)
        }
        editInputLayout?.boxStrokeColor = colorPrimary
        editInputLayout?.hintTextColor = ColorStateList.valueOf(colorPrimary)
    }

    override fun show() {
        super.show()
        layout?.visibility = View.VISIBLE
        acceptView.isEnabled = editValue?.isNotEmpty() == true
        dialog.view?.post {
            editValueView?.apply {
                if (isPassword) {
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    doOnTextChanged { _, _, _, _ ->
                        editInputLayout?.error = null
                        acceptView.isEnabled = !text?.trim().isNullOrEmpty()
                    }
                } else {
                    editValue?.let { value ->
                        setText(value)
                        if (!text.isNullOrEmpty()) {
                            setSelection(0, editValue?.length ?: 0)
                        }
                    }
                    filters = arrayOf<InputFilter>(EditFilter())
                }
            }

            editInputLayout?.apply {
                hint = hintValue.orEmpty()
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

            editValueView?.postDelayed({
                KeyboardUtils.showKeyboard(editValueView)
            }, 100)
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
            colorTint?.let { bundle.putInt(TAG_COLOR_TINT, it) }
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
            val checkedString = StringUtils.getAllowedString(mResultString)
            return if (checkedString != null) {
                errorValue = dialog.getString(R.string.dialogs_edit_forbidden_symbols)
                editInputLayout?.error = errorValue + StringUtils.DIALOG_FORBIDDEN_SYMBOLS
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

        fun setColorTint(int: Int?): Builder {
            colorTint = int
            return this
        }

        fun show() {
            dialog.show(CommonDialog.Dialogs.EDIT_LINE)
        }

    }

}