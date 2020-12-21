package lib.toolkit.base.ui.views.webview

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView

class KeyboardWebView : WebView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val inputConnection = super.onCreateInputConnection(outAttrs)

        //autocomplete disable, fix backspace
        outAttrs.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL

        return inputConnection
    }

}
