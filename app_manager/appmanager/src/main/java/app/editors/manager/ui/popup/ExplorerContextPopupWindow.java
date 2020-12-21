/*
 * Created by Michael Efremov on 29.07.20 12:47
 */

package app.editors.manager.ui.popup;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.snackbar.Snackbar;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.KeyboardUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.popup.BasePopup;

public class ExplorerContextPopupWindow extends BasePopup {

    protected PreferenceTool mPreferenceTool;

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

    protected ContextBottomDialog.State mState = new ContextBottomDialog.State();

    @Nullable
    private ContextBottomDialog.OnClickListener mListener;

    @OnClick({R.id.list_explorer_context_folder_name,
            R.id.list_explorer_context_edit,
            R.id.list_explorer_context_share,
            R.id.list_explorer_context_external_link,
            R.id.list_explorer_context_move,
            R.id.list_explorer_context_copy,
            R.id.list_explorer_context_download,
            R.id.list_explorer_context_rename,
            R.id.list_explorer_context_delete,
            R.id.list_explorer_context_share_delete})
    protected void onButtonsClick(final View view) {
        switch (view.getId()) {
            case R.id.list_explorer_context_folder_name:
                clickItem(ContextBottomDialog.Buttons.FOLDER);
                break;
            case R.id.list_explorer_context_edit:
                clickItem(ContextBottomDialog.Buttons.EDIT);
                break;
            case R.id.list_explorer_context_share:
                clickItem(ContextBottomDialog.Buttons.SHARE);
                break;
            case R.id.list_explorer_context_external_link:
                clickItem(ContextBottomDialog.Buttons.EXTERNAL);
                break;
            case R.id.list_explorer_context_move:
                clickItem(ContextBottomDialog.Buttons.MOVE);
                break;
            case R.id.list_explorer_context_copy:
                clickItem(ContextBottomDialog.Buttons.COPY);
                break;
            case R.id.list_explorer_context_download:
                clickItem(ContextBottomDialog.Buttons.DOWNLOAD);
                break;
            case R.id.list_explorer_context_rename:
                clickItem(ContextBottomDialog.Buttons.RENAME);
                break;
            case R.id.list_explorer_context_delete:
                clickItem(ContextBottomDialog.Buttons.DELETE);
                break;
            case R.id.list_explorer_context_share_delete:
                clickItem(ContextBottomDialog.Buttons.SHARE_DELETE);
                break;
        }
    }

    public ExplorerContextPopupWindow(@NonNull Context context) {
        super(context, R.layout.explorer_context_popup_layout);

    }

    @Override
    protected void bind(@NonNull View view) {
        mPopupWindow.setAnimationStyle(-1);
        mUnbinder = ButterKnife.bind(this, view);
        mPreferenceTool = App.getApp().getAppComponent().getPreference();
    }

    @Override
    public void hide() {
        super.hide();
        mListener = null;
    }

//    @Override
//    public void showDropAt(@NotNull View view, @Nullable Rect restrictRect) {
//        mPopupWindow = new PopupWindow(mPopupView, mPopupView.getMeasuredWidth(), mPopupView.getMeasuredHeight());
//        mPopupWindow.setOutsideTouchable(true);
//        mPopupWindow.setClippingEnabled(true);
//        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//        mPopupWindow.setElevation(view.getContext().getResources().getDimension(R.dimen.elevation_height_micro));
//        super.showDropAt(view, restrictRect);
//    }

    public void setViews() {
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

        if (mState.mIsTrash) {
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

        } else {
            // File can downloaded
            mListExplorerContextDownload.setVisibility(View.VISIBLE);

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
            info = mPopupView.getContext().getString(R.string.this_device) + mPopupView.getContext().getString(R.string.placeholder_point) + mState.mInfo;
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
        AccountsSqlData account = App.getApp().getAppComponent().getAccountsSql().getAccountOnline();
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

    public void setState(@NonNull final ContextBottomDialog.State state) {
        mState = state;
        setItemSharedState(state.mIsShared);
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
        Snackbar snackBar = UiUtils.getShortSnackBar(mPopupView);
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
            mPopupView.getContext().startActivity(Intent.createChooser(intent, mPopupView.getContext().getString(R.string.operation_snackbar_send_link)));
        });
    }

    public void setListener(@Nullable ContextBottomDialog.OnClickListener mListener) {
        this.mListener = mListener;
    }

    private void clickItem(ContextBottomDialog.Buttons button) {
        if (mListener != null) {
            mListener.onContextButtonClick(button);
            hide();
        }
    }
}
