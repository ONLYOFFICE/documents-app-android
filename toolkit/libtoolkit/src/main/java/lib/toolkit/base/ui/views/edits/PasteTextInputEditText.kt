package lib.toolkit.base.ui.views.edits

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import lib.toolkit.base.managers.utils.KeyboardUtils

class PasteTextInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            android.R.id.paste -> {
                this.setText(KeyboardUtils.getTextFromClipboard(context))
                return true
            }
        }
        return super.onTextContextMenuItem(id)
    }

}