package app.editors.manager.ui.views.edits;

import android.text.Editable;
import android.text.TextWatcher;

public class BaseWatcher implements TextWatcher {

    protected String mBeforeChangeText;
    protected String mOnChangeText;
    protected String mInputText;
    protected String mDeletedText;

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        mBeforeChangeText = text.toString();
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        mOnChangeText = text.toString();
        if (count - before > 0) {
            mInputText = mOnChangeText.substring(before, count);
            mDeletedText = "";
        } else {
            mInputText = "";
            mDeletedText = mBeforeChangeText.substring(count, before);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
