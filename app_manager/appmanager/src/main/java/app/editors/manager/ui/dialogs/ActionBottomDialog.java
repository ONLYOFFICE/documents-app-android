package app.editors.manager.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.editors.manager.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog;

public class ActionBottomDialog extends BaseBottomDialog {

    public static final String TAG = ActionBottomDialog.class.getSimpleName();
    private static final String TAG_THIRD_PARTY = "TAG_THIRD_PARTY";
    private static final String TAG_DOCS = "TAG_DOCS";
    private static final String TAG_LOCAL = "TAG_LOCAL";

    public enum Buttons {
        NONE, SHEET, PRESENTATION, DOC, FOLDER, PHOTO, UPLOAD, STORAGE
    }

    public interface OnClickListener {
        void onActionButtonClick(Buttons buttons);
    }

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
    private OnClickListener mOnClickListener;
    private boolean mIsThirdParty = false;
    private boolean mIsDocs = true;
    private boolean mIsLocal = false;
    private boolean mIsWebDav = false;

    public static ActionBottomDialog newInstance() {
        return new ActionBottomDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        restoreValues(savedInstanceState);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAG_THIRD_PARTY, mIsThirdParty);
        outState.putBoolean(TAG_DOCS, mIsDocs);
        outState.putBoolean(TAG_LOCAL, mIsLocal);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        init(dialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        mOnClickListener = null;
    }

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
                    mOnClickListener.onActionButtonClick(Buttons.SHEET);
                    break;
                case R.id.list_explorer_action_presentation:
                    mOnClickListener.onActionButtonClick(Buttons.PRESENTATION);
                    break;
                case R.id.list_explorer_action_docs:
                    mOnClickListener.onActionButtonClick(Buttons.DOC);
                    break;
                case R.id.list_explorer_action_folder:
                    mOnClickListener.onActionButtonClick(Buttons.FOLDER);
                    break;
                case R.id.list_explorer_action_photo:
                    mOnClickListener.onActionButtonClick(Buttons.PHOTO);
                    break;
                case R.id.list_explorer_action_upload:
                    mOnClickListener.onActionButtonClick(Buttons.UPLOAD);
                    break;
                case R.id.list_explorer_action_storage:
                    mOnClickListener.onActionButtonClick(Buttons.STORAGE);
                    break;
            }
            dismiss();
        }
    }

    private void restoreValues(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsThirdParty = savedInstanceState.getBoolean(TAG_THIRD_PARTY);
            mIsDocs = savedInstanceState.getBoolean(TAG_DOCS);
            mIsLocal = savedInstanceState.getBoolean(TAG_LOCAL);
        }
    }

    private void init(final Dialog dialog) {
        final View contentView = View.inflate(getContext(), R.layout.list_explorer_action_menu, null);
        mUnbinder = ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
        dialog.setCanceledOnTouchOutside(true);
        setViewState();
    }

    private void setViewState() {
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

    public void setOnClickListener(final OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
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
}