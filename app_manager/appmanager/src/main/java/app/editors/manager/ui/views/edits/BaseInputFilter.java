package app.editors.manager.ui.views.edits;

import android.text.InputFilter;
import android.text.Spanned;

public class BaseInputFilter implements InputFilter {

    protected String mResultString;

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        final StringBuilder resultStr = new StringBuilder(dest);
        resultStr.replace(dstart, dend, source.toString());
        mResultString = resultStr.toString();
        return null;
    }

}
