package app.editors.manager.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;

import app.documents.core.settings.NetworkSettings;
import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.KeyboardUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog;

public class ContextBottomDialog extends BaseBottomDialog {

    public static final String TAG = ContextBottomDialog.class.getSimpleName();
    private static final String TAG_STATE = "TAG_STATE";

    public enum Buttons {
        NONE, FOLDER, EDIT, SHARE, EXTERNAL, MOVE, COPY, DOWNLOAD, RENAME, DELETE, SHARE_DELETE, FAVORITE_ADD, FAVORITE_DELETE
    }

    public interface OnClickListener {
        void onContextButtonClick(Buttons buttons);
        void onContextDialogClose();
    }

    public static class State implements Serializable {
        public String mTitle = "";
        public String mInfo = "";
        public int mIconResId = 0;
        public boolean mIsFolder = false;
        public boolean mIsShared = false;
        public boolean mIsCanShare = false;
        public boolean mIsDocs = false;
        public boolean mIsStorage = false;
        public boolean mIsItemEditable = false;
        public boolean mIsContextEditable = false;
        public boolean mIsDeleteShare = false;
        public boolean mIsPdf = false;
        public boolean mIsLocal = false;
        public boolean mIsRecent = false;
        public boolean mIsWebDav = false;
        public boolean mIsTrash = false;
        public boolean mIsFavorite = false;
    }

    protected PreferenceTool mPreferenceTool;
    protected NetworkSettings mNetworkSettings;

    protected Unbinder mUnbinder;
    @BindView(R.id.list_explorer_context_folder_name)
    protected RelativeLayout mListContextFolderName;
    @BindView(R.id.list_explorer_context_header_image)
    protected AppCompatImageView mListContextHeaderImage;
    @BindView(R.id.list_explorer_context_header_title_text)
    protected AppCompatTextView mListContextHeaderTitle;
    @BindView(R.id.list_explorer_context_header_info_text)
    protected AppCompatTextView mListContextHeaderInfo;
    @BindView(R.id.list_explorer_context_edit)
    protected LinearLayout mListExplorerContextEdit;
    @BindView(R.id.list_explorer_context_share)
    protected LinearLayout mListContextShare;
    @BindView(R.id.list_explorer_context_external_link)
    protected RelativeLayout mListContextExternalLink;
    @BindView(R.id.list_explorer_context_move)
    protected LinearLayout mListContextMove;
    @BindView(R.id.list_explorer_context_download)
    protected LinearLayout mListExplorerContextDownload;
    @BindView(R.id.list_explorer_context_copy)
    protected LinearLayout mListContextCopy;
    @BindView(R.id.list_explorer_context_rename)
    protected LinearLayout mListContextRename;
    @BindView(R.id.view_line_separator_delete)
    protected View mViewLineSeparatorDelete;
    @BindView(R.id.view_line_separator_edit)
    protected View mViewLineSeparatorEdit;
    @BindView(R.id.view_line_separator_share)
    protected View mViewLineSeparatorShare;
    @BindView(R.id.list_explorer_context_delete)
    protected LinearLayout mListContextDelete;
    @BindView(R.id.list_explorer_context_share_delete)
    protected LinearLayout mListContextShareDelete;
    @BindView(R.id.list_explorer_context_delete_text)
    protected AppCompatTextView mListExplorerContextDeleteText;
    @BindView(R.id.list_explorer_context_add_to_favorite)
    protected LinearLayout mListContextAddFavorite;
    @BindView(R.id.list_explorer_context_delete_from_favorite)
    protected LinearLayout mListContextDeleteFavorite;
    @BindView(R.id.view_line_separator_favorites)
    protected View mViewLineSeparatorFavorites;

    protected View mRootView;
    protected State mState = new State();
    protected OnClickListener mOnClickListener;

    public static ContextBottomDialog newInstance() {
        return new ContextBottomDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        mPreferenceTool = App.getApp().getAppComponent().getPreference();
        mNetworkSettings = App.getApp().getAppComponent().getNetworkSettings();
        restoreValues(savedInstanceState);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TAG_STATE, mState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        init(dialog, style);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mOnClickListener.onContextDialogClose();
    }

    @OnClick({R.id.list_explorer_context_folder_name,
            R.id.list_explorer_context_edit,
            R.id.list_explorer_context_share,
            R.id.list_explorer_context_external_link,
            R.id.list_explorer_context_move,
            R.id.list_explorer_context_copy,
            R.id.list_explorer_context_download,
            R.id.list_explorer_context_rename,
            R.id.list_explorer_context_delete,
            R.id.list_explorer_context_share_delete,
            R.id.list_explorer_context_add_to_favorite,
            R.id.list_explorer_context_delete_from_favorite})
    protected void onButtonsClick(final View view) {
        if (mOnClickListener != null) {
            switch (view.getId()) {
                case R.id.list_explorer_context_folder_name:
                    mOnClickListener.onContextButtonClick(Buttons.FOLDER);
                    break;
                case R.id.list_explorer_context_edit:
                    mOnClickListener.onContextButtonClick(Buttons.EDIT);
                    break;
                case R.id.list_explorer_context_share:
                    mOnClickListener.onContextButtonClick(Buttons.SHARE);
                    break;
                case R.id.list_explorer_context_external_link:
                    mOnClickListener.onContextButtonClick(Buttons.EXTERNAL);
                    break;
                case R.id.list_explorer_context_move:
                    mOnClickListener.onContextButtonClick(Buttons.MOVE);
                    break;
                case R.id.list_explorer_context_copy:
                    mOnClickListener.onContextButtonClick(Buttons.COPY);
                    break;
                case R.id.list_explorer_context_download:
                    mOnClickListener.onContextButtonClick(Buttons.DOWNLOAD);
                    break;
                case R.id.list_explorer_context_rename:
                    mOnClickListener.onContextButtonClick(Buttons.RENAME);
                    break;
                case R.id.list_explorer_context_delete:
                    mOnClickListener.onContextButtonClick(Buttons.DELETE);
                    break;
                case R.id.list_explorer_context_share_delete:
                    mOnClickListener.onContextButtonClick(Buttons.SHARE_DELETE);
                    break;
                case R.id.list_explorer_context_add_to_favorite:
                    mOnClickListener.onContextButtonClick(Buttons.FAVORITE_ADD);
                    break;
                case R.id.list_explorer_context_delete_from_favorite:
                    mOnClickListener.onContextButtonClick(Buttons.FAVORITE_DELETE);
                    break;
            }
            dismiss();
        }
    }

    private void restoreValues(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable(TAG_STATE);
        }
    }

    private void init(final Dialog dialog, final int style) {
        mRootView = View.inflate(getContext(), R.layout.list_explorer_context_menu, null);
        dialog.setContentView(mRootView);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mListContextHeaderTitle.setText(mState.mTitle);
        mListContextHeaderInfo.setText(mState.mInfo);
        mListContextHeaderImage.setImageResource(mState.mIconResId);
        UiUtils.setImageTint(mListContextHeaderImage, R.color.colorGrey);
        setViewState();
    }

    private void setViewState() {
        if (mState.mIsRecent) {
            setRecentState();
            return;
        }

        if (mState.mIsTrash){
            setTrashState();
            return;
        }

        // Common
        mListContextCopy.setVisibility(View.VISIBLE);

        if (mState.mIsWebDav) {
            setWebDav();
            return;
        }
        if (mState.mIsLocal) {
            setLocalState();
            return;
        }

        // Folders or Files
        if (mState.mIsFolder) {
            // Folder is storage
            if (mState.mIsStorage) {
                mListExplorerContextDeleteText.setText(R.string.list_context_delete_storage);
            } else {
                mListExplorerContextDeleteText.setText(R.string.list_context_delete);
            }

            mListExplorerContextDownload.setVisibility(View.VISIBLE);

        } else {
            // File can downloaded
            mListExplorerContextDownload.setVisibility(View.VISIBLE);
            if(StringUtils.convertServerVersion(mNetworkSettings.getServerVersion()) >= 11 && mPreferenceTool.isFavoritesEnabled()) {
                mViewLineSeparatorFavorites.setVisibility(View.VISIBLE);
                if (mState.mIsFavorite) {
                    mListContextDeleteFavorite.setVisibility(View.VISIBLE);
                } else {
                    mListContextAddFavorite.setVisibility(View.VISIBLE);
                }
            }

            // File is document
            if (mState.mIsDocs && !mState.mIsPdf) {
                if (mState.mIsItemEditable) {
                    mViewLineSeparatorEdit.setVisibility(View.VISIBLE);
                    mListExplorerContextEdit.setVisibility(View.VISIBLE);
                }
            }

            // File can access by link
            if (mState.mIsCanShare) {
                mListContextExternalLink.setVisibility(View.VISIBLE);
            }
        }

        // Folders and files
        // Context is editable
        if (mState.mIsContextEditable) {
            mListContextMove.setVisibility(View.VISIBLE);
            setDeleteVisibility(View.VISIBLE);
        }

        // Item can edit
        if (mState.mIsItemEditable) {
            mListContextRename.setVisibility(View.VISIBLE);
        }

        // Item can share
        if (mState.mIsCanShare) {
            mViewLineSeparatorShare.setVisibility(View.VISIBLE);
            mListContextShare.setVisibility(View.VISIBLE);
        }

        // Only for share section, instead of delete
        if (mState.mIsDeleteShare) {
            mListContextShareDelete.setVisibility(View.VISIBLE);
        }

        if (mPreferenceTool.isPersonalPortal() && !mState.mIsFolder) {
            mViewLineSeparatorShare.setVisibility(View.VISIBLE);
            mListContextExternalLink.setVisibility(View.VISIBLE);
        }
    }

    private void setTrashState() {
        mListContextMove.setVisibility(View.VISIBLE);
        mListContextDelete.setVisibility(View.VISIBLE);
    }

    private void setRecentState() {
        String info;
        if (mState.mIsLocal) {
            info = getString(R.string.this_device) + getString(R.string.placeholder_point) + mState.mInfo;
        } else {
            info = mState.mInfo;
        }

        mListContextHeaderInfo.setText(info);
        mListContextCopy.setVisibility(View.GONE);
        mListExplorerContextDownload.setVisibility(View.GONE);
        ((AppCompatTextView) mListContextDelete.findViewById(R.id.list_explorer_context_delete_text)).setText(R.string.list_context_delete_recent);
        mListContextDelete.setVisibility(View.VISIBLE);
    }

    private void setWebDav() {
        mListContextDelete.setVisibility(View.VISIBLE);
        mListContextMove.setVisibility(View.VISIBLE);
        mListContextRename.setVisibility(View.VISIBLE);
        if (mState.mIsFolder) {
            mListExplorerContextDownload.setVisibility(View.GONE);
        } else {
            mListExplorerContextDownload.setVisibility(View.VISIBLE);
        }
        mViewLineSeparatorDelete.setVisibility(View.VISIBLE);
    }

    private void setUploadToPortal(boolean isVisible) {
        if (isVisible) {
            ((AppCompatImageView) mListExplorerContextDownload.findViewById(R.id.context_download_image)).setImageResource(R.drawable.ic_list_action_upload);
            ((AppCompatTextView) mListExplorerContextDownload.findViewById(R.id.context_download_text)).setText(R.string.list_context_upload_to_portal);
            mListExplorerContextDownload.setVisibility(View.VISIBLE);
        } else {
            mListExplorerContextDownload.setVisibility(View.GONE);
        }
    }

    private void setLocalState() {
        AccountsSqlData account = null;
        setUploadToPortal(account != null && account.isOnline() && !mState.mIsFolder);
        mListContextMove.setVisibility(View.VISIBLE);
        mListContextCopy.setVisibility(View.VISIBLE);
        mListContextDelete.setVisibility(View.VISIBLE);
        mListContextRename.setVisibility(View.VISIBLE);
        setDeleteVisibility(View.VISIBLE);
    }

    private void setDeleteVisibility(final int visibility) {
        mViewLineSeparatorDelete.setVisibility(visibility);
        mListContextDelete.setVisibility(visibility);
    }

    public void setState(@NonNull final State state) {
        mState = state;
        setItemSharedState(state.mIsShared);
    }

    public void setOnClickListener(@Nullable final OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setItemSharedState(final boolean isShared) {
        mState.mIsShared = isShared;
    }

    public void setItemSharedEnable(final boolean isEnable) {
        if (mListContextExternalLink != null) {
            mListContextExternalLink.setEnabled(isEnable);
        }
    }

    public void showMessage(@NonNull final String message) {
        Snackbar snackBar = UiUtils.getShortSnackBar(mRootView);
        if (mState.mIsShared) {
            showSendLinkButton(snackBar);
        }
        snackBar.setText(message).show();
    }

    private void showSendLinkButton(Snackbar snackBar) {
        snackBar.setAction(R.string.operation_snackbar_send_link, v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_TEXT, KeyboardUtils.getTextFromClipboard(App.getApp()));
            startActivity(Intent.createChooser(intent, getString(R.string.operation_snackbar_send_link)));
        });
    }

}
