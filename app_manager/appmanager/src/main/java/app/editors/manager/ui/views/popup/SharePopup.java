package app.editors.manager.ui.views.popup;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import app.editors.manager.R;
import lib.toolkit.base.ui.popup.BasePopup;

public class SharePopup extends BasePopup {

    private PopupContextListener mContextListener;
    private boolean mIsFullAccess;

    private View mCommentView;
    private View mReviewView;
    private View mFillFormsView;
    private View mFullView;

    public SharePopup(@NonNull Context mContext, int mLayoutId) {
        super(mContext, mLayoutId);
    }

    @Override
    protected void bind(@NonNull View view) {
        mFullView = view.findViewById(R.id.popup_share_access_full);
        mFullView.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        mCommentView = view.findViewById(R.id.popup_share_access_comment);
        mCommentView.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        mFillFormsView = view.findViewById(R.id.popup_share_access_fill_forms);
        mFillFormsView.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        mReviewView = view.findViewById(R.id.popup_share_access_review);
        mReviewView.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        final View viewRead = view.findViewById(R.id.popup_share_access_read);
        viewRead.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        final View viewDeny = view.findViewById(R.id.popup_share_access_deny);
        viewDeny.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        final View viewRemove = view.findViewById(R.id.popup_share_access_remove);
        viewRemove.setOnClickListener(v -> mContextListener.onContextClick(v, this));
        final View viewSeparatorDeny = view.findViewById(R.id.popup_share_access_separator_deny);
        final View viewSeparatorRemove = view.findViewById(R.id.popup_share_access_separator_remove);

        if (mIsFullAccess) {
            viewSeparatorDeny.setVisibility(View.GONE);
            viewRemove.setVisibility(View.VISIBLE);
            viewSeparatorRemove.setVisibility(View.VISIBLE);
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void setFullAccess(boolean mIsFullAccess) {
        this.mIsFullAccess = mIsFullAccess;
    }

    public void setIsFolder(boolean mIsFolder) {
        if (mIsFolder) {
            mReviewView.setVisibility(View.GONE);
            mFillFormsView.setVisibility(View.GONE);
            mCommentView.setVisibility(View.GONE);
            mFullView.setVisibility(View.VISIBLE);
        }
    }

    public void setIsDoc(boolean mIsDoc) {
        if (!mIsDoc) {
            mReviewView.setVisibility(View.GONE);
            mFillFormsView.setVisibility(View.GONE);
            mFullView.setVisibility(View.VISIBLE);
        }
    }

    public void setIsVisitor() {
        mCommentView.setVisibility(View.GONE);
        mReviewView.setVisibility(View.GONE);
        mFillFormsView.setVisibility(View.GONE);
        mFullView.setVisibility(View.GONE);
    }

    public void setContextListener(PopupContextListener mContextListener) {
        this.mContextListener = mContextListener;
    }

    public interface PopupContextListener {
        void onContextClick(View v, SharePopup sharePopup);
    }

}
