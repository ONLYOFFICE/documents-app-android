package app.editors.manager.ui.views.popup;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import app.editors.manager.R;
import lib.toolkit.base.ui.popup.BasePopup;

public class TrashPopup extends BasePopup {

    private PopupContextListener mContextListener;
    private AppCompatTextView mMoveTitle;

    public interface PopupContextListener {
        void onContextClick(View v, TrashPopup trashPopup);
    }

    public TrashPopup(@NonNull Context mContext, int mLayoutId) {
        super(mContext, mLayoutId);
    }

    @Override
    protected void bind(@NonNull View view) {
        final View viewMove = view.findViewById(R.id.popup_trash_move);
        viewMove.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        mMoveTitle = viewMove.findViewById(R.id.popup_move_title);

        final View viewDelete = view.findViewById(R.id.popup_trash_delete);
        viewDelete.setOnClickListener(v -> mContextListener.onContextClick(v, this));
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void setMoveTitle(String title) {
        mMoveTitle.setText(title);
    }

    public void setContextListener(TrashPopup.PopupContextListener mContextListener) {
        this.mContextListener = mContextListener;
    }

}
