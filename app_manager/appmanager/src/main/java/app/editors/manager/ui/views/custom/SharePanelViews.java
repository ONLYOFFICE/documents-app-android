package app.editors.manager.ui.views.custom;


import android.app.Activity;
import android.text.Editable;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;

import app.documents.core.network.ApiContract;
import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.managers.exceptions.ButterknifeInitException;
import app.editors.manager.ui.views.animation.HeightValueAnimator;
import app.editors.manager.ui.views.edits.BaseWatcher;
import app.editors.manager.ui.views.popup.SharePopup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SharePanelViews implements HeightValueAnimator.OnAnimationListener {

    public interface OnEventListener {
        void onPanelAccessClick(int accessCode);

        void onPanelResetClick();

        void onPanelMessageClick(boolean isShow);

        void onPanelAddClick();

        void onMessageInput(String message);
    }

    @BindView(R.id.button_popup_layout)
    protected ConstraintLayout mShareAccessButtonLayout;
    @BindView(R.id.button_popup_image)
    protected AppCompatImageView mShareAccessButtonView;
    @BindView(R.id.button_popup_arrow)
    protected AppCompatImageView mShareAccessButtonArrow;

    @BindView(R.id.share_panel_message_edit_layout)
    protected TextInputLayout mSharePanelMessageEditLayout;
    @BindView(R.id.share_panel_message_edit)
    protected AppCompatEditText mSharePanelMessageEdit;
    @BindView(R.id.share_panel_reset_button)
    protected AppCompatImageButton mSharePanelResetButton;
    @BindView(R.id.share_panel_count_selected_text)
    protected AppCompatTextView mSharePanelCountSelectedText;
    @BindView(R.id.share_panel_message_button)
    protected AppCompatImageButton mSharePanelMessageButton;
    @BindView(R.id.share_panel_add_button)
    protected AppCompatButton mSharePanelAddButton;

    private View mView;
    private Unbinder mUnbinder;
    private HeightValueAnimator mHeightValueAnimator;
    private OnEventListener mOnEventListener;
    private SharePopup mSharePopup;
    private PopupAccessListener mPopupAccessListener;
    private Activity mActivity;

    public SharePanelViews(final View view, Activity activity) {
        try {
            mUnbinder = ButterKnife.bind(this, view);
        } catch (RuntimeException e) {
            throw new ButterknifeInitException(SharePanelViews.class.getSimpleName() + " - must initWithPreferences with specific view!", e);
        }

        mActivity = activity;
        mView = view;
        mHeightValueAnimator = new HeightValueAnimator(mSharePanelMessageEditLayout);
        mHeightValueAnimator.setOnAnimationListener(this);
        mPopupAccessListener = new PopupAccessListener();
        mSharePanelMessageEdit.addTextChangedListener(new FieldsWatcher());
    }

    @OnClick({R.id.button_popup_layout,
            R.id.share_panel_reset_button,
            R.id.share_panel_message_button,
            R.id.share_panel_add_button})
    public void onClickButtons(final View view) {
        switch (view.getId()) {
            case R.id.button_popup_layout:
                mSharePopup = new SharePopup(mView.getContext(), R.layout.popup_share_menu);
                mSharePopup.setContextListener(mPopupAccessListener);
                mSharePopup.setExternalLink();
                mSharePopup.setFullAccess(true);
                mSharePopup.showOverlap(view, mActivity);
                break;
            case R.id.share_panel_reset_button:
                onReset();
                break;
            case R.id.share_panel_message_button:
                onMessage();
                break;
            case R.id.share_panel_add_button:
                onAdd();
                break;
        }
    }

    @Override
    public void onStart(boolean isShow) {

    }

    @Override
    public void onEnd(boolean isShow) {

    }

    public void setCount(final int count) {
        if (count > 0) {
            mSharePanelResetButton.setClickable(true);
        } else {
            mSharePanelResetButton.setClickable(false);
        }
        mSharePanelCountSelectedText.setText(String.valueOf(count));
    }

    public void setAddButtonEnable(boolean isEnable) {
        mSharePanelAddButton.setEnabled(isEnable);
    }

    public void setOnEventListener(OnEventListener onEventListener) {
        mOnEventListener = onEventListener;
    }

    public boolean hideMessageView() {
        final boolean isShow = isMessageShowed();
        mHeightValueAnimator.animate(false);
        return isShow;
    }

    @Nullable
    public String getMessage() {
        if (mShareAccessButtonLayout.getVisibility() == View.VISIBLE) {
            final String message = mSharePanelMessageEdit.getText().toString().trim();
            if (!message.isEmpty()) {
                return message;
            }
        }

        return null;
    }

    private void onReset() {
        mSharePanelCountSelectedText.setText(String.valueOf(0));
        mSharePanelAddButton.setEnabled(false);
        if (mOnEventListener != null) {
            mOnEventListener.onPanelResetClick();
        }
    }

    private void onPopupAccess(final int accessCode) {
        hideMessageView();
        setAccessIcon(accessCode);
        if (mOnEventListener != null) {
            mOnEventListener.onPanelAccessClick(accessCode);
        }
    }

    private void onMessage() {
        final boolean isShowMessage = !isMessageShowed();
        mSharePanelMessageEdit.setText("");

        mHeightValueAnimator.animate(isShowMessage);

        if (mOnEventListener != null) {
            mOnEventListener.onPanelMessageClick(isShowMessage);
        }
    }

    private boolean isMessageShowed() {
        return mSharePanelMessageEditLayout.getVisibility() == View.VISIBLE;
    }

    private void onAdd() {
        hideMessageView();
        if (mOnEventListener != null) {
            mOnEventListener.onPanelAddClick();
        }
    }

    public void setAccessIcon(final int accessCode) {
        switch (accessCode) {
            case ApiContract.ShareCode.NONE:
                mShareAccessButtonView.setImageResource(R.drawable.ic_access_deny);
                break;
            case ApiContract.ShareCode.READ:
                mShareAccessButtonView.setImageResource(R.drawable.ic_access_read);
                break;
            case ApiContract.ShareCode.READ_WRITE:
                mShareAccessButtonView.setImageResource(R.drawable.ic_access_full);
                break;
            case ApiContract.ShareCode.REVIEW:
                mShareAccessButtonView.setImageResource(R.drawable.ic_access_review);
                break;
            case ApiContract.ShareCode.COMMENT:
                mShareAccessButtonView.setImageResource(R.drawable.ic_access_comment);
                break;
            case ApiContract.ShareCode.FILL_FORMS:
                mShareAccessButtonView.setImageResource(R.drawable.ic_access_fill_form);
                break;
        }
    }

    public boolean popupDismiss() {
        if (mSharePopup != null && mSharePopup.isShowing()) {
            mSharePopup.hide();
            return true;
        }

        return false;
    }

    public void unbind() {
        mHeightValueAnimator.clear();
        mUnbinder.unbind();
    }

    /*
     * Popup callbacks
     * */
    private class PopupAccessListener implements SharePopup.PopupContextListener {

        @Override
        public void onContextClick(View v, SharePopup sharePopup) {
            sharePopup.hide();
            switch (v.getId()) {
                case R.id.popup_share_access_full:
                    onPopupAccess(ApiContract.ShareCode.READ_WRITE);
                    break;
                case R.id.popup_share_access_review:
                    onPopupAccess(ApiContract.ShareCode.REVIEW);
                    break;
                case R.id.popup_share_access_read:
                    onPopupAccess(ApiContract.ShareCode.READ);
                    break;
                case R.id.popup_share_access_deny:
                    onPopupAccess(ApiContract.ShareCode.NONE);
                    break;
                case R.id.popup_share_access_comment:
                    onPopupAccess(ApiContract.ShareCode.COMMENT);
                    break;
                case R.id.popup_share_access_fill_forms:
                    onPopupAccess(ApiContract.ShareCode.FILL_FORMS);
                    break;
            }
        }
    }

    /*
     * Text input listener
     * */
    private class FieldsWatcher extends BaseWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            if (mOnEventListener != null) {
                mOnEventListener.onMessageInput(s.toString());
            }
        }
    }

}

