package app.editors.manager.ui.views.edits;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import lib.toolkit.base.managers.utils.KeyboardUtils;

public class BaseEditText extends AppCompatEditText {

    public interface OnContextMenu {
        boolean onTextPaste(@Nullable String text);
    }

    @Nullable
    protected OnContextMenu mOnContextMenu;

    public BaseEditText(Context context) {
        super(context);
    }

    public BaseEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (mOnContextMenu != null) {
            switch(id) {
                case android.R.id.paste:
                    if (mOnContextMenu.onTextPaste(KeyboardUtils.getTextFromClipboard(getContext()))) {
                        return true;
                    }
                    break;
            }
        }

        return super.onTextContextMenuItem(id);
    }

    public void setOnContextMenu(@Nullable OnContextMenu onContextMenu) {
        mOnContextMenu = onContextMenu;
    }

}
