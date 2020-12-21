package app.editors.manager.ui.views.webview;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class KeyboardWebView extends WebView {

    public KeyboardWebView(Context context) {
        super(context);
    }

    public KeyboardWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KeyboardWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection inputConnection = super.onCreateInputConnection(outAttrs);

        //autocomplete disable, fix backspace
        outAttrs.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL;

        return inputConnection;
    }

}
