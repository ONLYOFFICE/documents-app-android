/*
 * Created by Michael Efremov on 05.08.20 12:20
 */

package app.editors.manager.ui.dialogs;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.ui.popup.BasePopup;

public class ActionBottomPopup extends BasePopup {

    public static final String TAG = ActionBottomPopup.class.getSimpleName();

    @BindView(R.id.list_explorer_action_sheet)
    protected LinearLayout mListActionSheet;
    @BindView(R.id.list_explorer_action_presentation)
    protected LinearLayout mListActionPresentation;
    @BindView(R.id.list_explorer_action_docs)
    protected LinearLayout mListActionDocs;
    @BindView(R.id.list_explorer_action_folder)
    protected LinearLayout mListActionFolder;
    @BindView(R.id.list_explorer_action_photo)
    protected LinearLayout mListActionPhoto;
    @BindView(R.id.list_explorer_action_upload)
    protected LinearLayout mListActionUpload;
    @BindView(R.id.view_line_separator_storage)
    protected View mViewLineSeparatorStorage;
    @BindView(R.id.list_explorer_action_storage)
    protected LinearLayout mListActionStorage;

    @Nullable
    private Unbinder mUnbinder;
    @Nullable
    private ActionBottomDialog.OnClickListener mOnClickListener;
    private boolean mIsThirdParty = false;
    private boolean mIsDocs = true;
    private boolean mIsLocal = false;
    private boolean mIsWebDav = false;

    private ViewTreeObserver.OnGlobalLayoutListener mListener;

    @OnClick({R.id.list_explorer_action_sheet,
            R.id.list_explorer_action_presentation,
            R.id.list_explorer_action_docs,
            R.id.list_explorer_action_folder,
            R.id.list_explorer_action_photo,
            R.id.list_explorer_action_upload,
            R.id.list_explorer_action_storage})
    public void onButtonsClick(View view) {
        if (mOnClickListener != null) {
            switch (view.getId()) {
                case R.id.list_explorer_action_sheet:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.SHEET);
                    break;
                case R.id.list_explorer_action_presentation:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.PRESENTATION);
                    break;
                case R.id.list_explorer_action_docs:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.DOC);
                    break;
                case R.id.list_explorer_action_folder:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.FOLDER);
                    break;
                case R.id.list_explorer_action_photo:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.PHOTO);
                    break;
                case R.id.list_explorer_action_upload:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.UPLOAD);
                    break;
                case R.id.list_explorer_action_storage:
                    mOnClickListener.onActionButtonClick(ActionBottomDialog.Buttons.STORAGE);
                    break;
            }
            hide();
        }
    }

    public ActionBottomPopup(@NonNull Context context) {
        super(context, R.layout.action_bottom_popup_layout);
    }

    @Override
    protected void bind(@NonNull View view) {
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void hide() {
        super.hide();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        mOnClickListener = null;
    }

//    @Override
//    public void showDropAt(@NonNull View view) {
//        super.showDropAt(view);
//        mListener  = () -> {
//            mPopupView.getViewTreeObserver().removeOnGlobalLayoutListener(mListener);
//
//            final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
//            final int offset = (int) UiUtils.dpToPixel(params.width + params.rightMargin, view.getContext());
//            mPopupWindow.update(view, -offset * 2, 0, mPopupView.getMeasuredWidth(), mPopupView.getMeasuredHeight());
//
//        };
//        mPopupView.getViewTreeObserver().addOnGlobalLayoutListener(mListener);
//    }

    public void setViewState() {
        popupWindow.setAnimationStyle(R.style.FabPopupAnimation);

        if (!mIsThirdParty) {
            mViewLineSeparatorStorage.setVisibility(View.GONE);
            mListActionStorage.setVisibility(View.GONE);
        }

        if (!mIsDocs) {
            mListActionDocs.setVisibility(View.GONE);
            mListActionPresentation.setVisibility(View.GONE);
            mListActionSheet.setVisibility(View.GONE);
        }

        if (mIsLocal) {
            mListActionDocs.setVisibility(View.VISIBLE);
            mListActionPresentation.setVisibility(View.VISIBLE);
            mListActionSheet.setVisibility(View.VISIBLE);
            if (mIsWebDav) {
                mListActionUpload.setVisibility(View.VISIBLE);
            } else {
                mListActionUpload.setVisibility(View.GONE);
            }
        }
    }

    public void setOnClickListener(@Nullable ActionBottomDialog.OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public void setThirdParty(boolean thirdParty) {
        mIsThirdParty = thirdParty;
    }

    public void setDocs(boolean docs) {
        mIsDocs = docs;
    }

    public void setLocal(boolean isLocal) {
        mIsLocal = isLocal;
    }

    public void setWebDav(boolean mIsWebDav) {
        this.mIsWebDav = mIsWebDav;
    }

    public boolean isVisible() {
        return popupWindow.isShowing();
    }
}
