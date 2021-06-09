package app.editors.manager.ui.fragments.main;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import app.documents.core.network.ApiContract;
import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.CloudFile;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.presenters.main.DocsBasePresenter;
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter;
import app.editors.manager.mvp.views.main.DocsBaseView;
import app.editors.manager.mvp.views.main.DocsCloudView;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.activities.main.ShareActivity;
import app.editors.manager.ui.activities.main.StorageActivity;
import app.editors.manager.ui.dialogs.ActionBottomDialog;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.dialogs.MoveCopyDialog;
import lib.toolkit.base.managers.utils.TimeUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public abstract class DocsCloudFragment extends DocsBaseFragment implements DocsCloudView {

    protected static String KEY_ACCOUNT = "key_account";

    @InjectPresenter
    public DocsCloudPresenter mCloudPresenter;
    @ProvidePresenter
    public DocsCloudPresenter providePresenter() {
        return new DocsCloudPresenter(getArguments().getString(KEY_ACCOUNT));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BaseAppActivity.REQUEST_ACTIVITY_WEB_VIEWER: {
                    showViewerActivity((CloudFile) data.getSerializableExtra("TAG_FILE"));
                    break;
                }

                case BaseAppActivity.REQUEST_ACTIVITY_OPERATION: {
                    showSnackBar(R.string.operation_complete_message);
                    onRefresh();
                    break;
                }
                case BaseAppActivity.REQUEST_ACTIVITY_STORAGE: {
                    final CloudFolder folder = (CloudFolder) data.getSerializableExtra(StorageActivity.TAG_RESULT);
                    mCloudPresenter.addFolderAndOpen(folder, mLinearLayoutManager.findFirstVisibleItemPosition());
                    break;
                }
                case BaseAppActivity.REQUEST_ACTIVITY_SHARE: {
                    if (data != null && data.hasExtra(ShareActivity.TAG_RESULT)) {
                        mCloudPresenter.setItemsShared(data.getBooleanExtra(ShareActivity.TAG_RESULT, false));
                    }
                    break;
                }
                case BaseAppActivity.REQUEST_ACTIVITY_CAMERA: {
                    if (mCameraUri != null) {
                        mCloudPresenter.upload(mCameraUri, null);
                    }
                    break;
                }
                case BaseAppActivity.REQUEST_ACTIVITY_FILE_PICKER: {
                    if (data != null && data.getClipData() != null) {
                        mCloudPresenter.upload(null, data.getClipData());
                    }
                    if (data != null && data.getData() != null) {
                        mCloudPresenter.upload(data.getData(), null);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onActionBarTitle(String title) {
        if (isActivePage()) {
            setActionBarTitle(title);
            if (title.equals("0")) {
                disableMenu();
            }
        }
    }

    @Override
    public void onStateMenuSelection() {
        if (mMenu != null && mMenuInflater != null) {
            mMenuInflater.inflate(R.menu.docs_select, mMenu);
            mDeleteItem = mMenu.findItem(R.id.toolbar_selection_delete).setVisible(mCloudPresenter.isContextItemEditable());
            mMoveItem = mMenu.findItem(R.id.toolbar_selection_move).setVisible(mCloudPresenter.isContextItemEditable());
            mCopyItem = mMenu.findItem(R.id.toolbar_selection_copy);
            mDownloadItem = mMenu.findItem(R.id.toolbar_selection_download).setVisible(!mCloudPresenter.isTrashMode());
            UiUtils.setMenuItemTint(requireContext(), mDeleteItem, R.color.colorWhite);
            setAccountEnable(false);
        }
    }

    @Override
    public void onUploadFileProgress(int progress, String id) {
        UploadFile uploadFile = mExplorerAdapter.getUploadFileById(id);
        if(uploadFile != null) {
            uploadFile.setProgress(progress);
            mExplorerAdapter.updateItem(uploadFile);
        }
    }

    @Override
    public void onDeleteUploadFile(String id) {
        mExplorerAdapter.removeUploadItemById(id);
    }

    @Override
    public void onRemoveUploadHead() {
        mExplorerAdapter.removeHeader(App.getApp().getString(R.string.upload_manager_progress_title));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAddUploadsFile(List<? extends Entity> uploadFiles) {
        onRemoveUploadHead();
        mExplorerAdapter.addItemsAtTop((List<Entity>) uploadFiles);
        mExplorerAdapter.addItemAtTop(new Header(getString(R.string.upload_manager_progress_title)));
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void showMoveCopyDialog(ArrayList<String> names, String action, String titleFolder) {
        mMoveCopyDialog = MoveCopyDialog.newInstance(names, action, titleFolder);
        mMoveCopyDialog.setOnClick(this);
        mMoveCopyDialog.show(requireFragmentManager(), MoveCopyDialog.TAG);
    }

    @Override
    public void onActionButtonClick(ActionBottomDialog.Buttons buttons) {
        super.onActionButtonClick(buttons);
        switch (buttons) {
            case PHOTO:
                if (checkCameraPermission()) {
                    showCameraActivity(TimeUtils.getFileTimeStamp());
                }
                break;
            case STORAGE: {
                showStorageActivity(mCloudPresenter.isUserSection());
                //TODO show user info about third party storage location
            }
        }
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            switch (tag) {
                case DocsBasePresenter.TAG_DIALOG_BATCH_EMPTY:
                    mCloudPresenter.emptyTrash();
                    break;
                case DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE:
                    mCloudPresenter.removeShareContext();
                    break;
            }
        }
    }

    @Override
    public void onContextButtonClick(ContextBottomDialog.Buttons buttons) {
        super.onContextButtonClick(buttons);
        switch (buttons) {
            case EDIT:
                mCloudPresenter.onEditContextClick();
                break;
            case SHARE:
                showShareActivity(mCloudPresenter.getItemClicked());
                break;
            case EXTERNAL:
                setContextDialogExternalLinkEnable(false);
                mCloudPresenter.getExternalLink();
                break;
            case SHARE_DELETE:
                showQuestionDialog(getString(R.string.dialogs_question_share_remove), mCloudPresenter.getItemTitle(),
                        getString(R.string.dialogs_question_share_remove), getString(R.string.dialogs_common_cancel_button),
                        DocsBasePresenter.TAG_DIALOG_CONTEXT_SHARE_DELETE);
                break;
            case FAVORITE_ADD:
                mCloudPresenter.addToFavorite();
                break;
            case FAVORITE_DELETE:
                mCloudPresenter.deleteFromFavorite();
                break;
        }
    }

    @Override
    public void continueClick(@NonNull String tag, String action) {
        int operationType = ApiContract.Operation.OVERWRITE;
        switch (tag) {
            case MoveCopyDialog.TAG_DUPLICATE:
                operationType = ApiContract.Operation.DUPLICATE;
                break;
            case MoveCopyDialog.TAG_OVERWRITE:
                operationType = ApiContract.Operation.OVERWRITE;
                break;
            case MoveCopyDialog.TAG_SKIP:
                operationType = ApiContract.Operation.SKIP;
                break;
        }
        if (action.equals(MoveCopyDialog.ACTION_COPY)) {
            mCloudPresenter.transfer(operationType, false);
        } else {
            mCloudPresenter.transfer(operationType, true);
        }
    }

    @Override
    protected boolean isActivePage() {
            final Fragment fragment = getParentFragment();
            if (fragment instanceof MainPagerFragment) {
                return ((MainPagerFragment) fragment).isActivePage(this);
            } else {
                return true;
            }

    }

    @Override
    public void onFileWebView(CloudFile file) {
        showViewerActivity(file);
    }

    @Override
    protected DocsBasePresenter<? extends DocsBaseView> getPresenter() {
        return mCloudPresenter;
    }

    /*
     * On pager scroll callback
     * */
    public void onScrollPage() {
        mCloudPresenter.initViews();
    }

    private void disableMenu() {
        if (mMenu != null) {
            mDeleteItem.setEnabled(false);
        }
    }

    @Override
    protected Boolean isWebDav() {
        return false;
    }

    public boolean isRoot() {
        return getPresenter().isRoot();
    }
}
