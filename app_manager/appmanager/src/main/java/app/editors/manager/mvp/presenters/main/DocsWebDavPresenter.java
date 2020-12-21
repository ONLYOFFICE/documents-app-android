package app.editors.manager.mvp.presenters.main;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.providers.BaseFileProvider;
import app.editors.manager.managers.providers.WebDavFileProvider;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.models.ModelExplorerStack;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.views.main.DocsWebDavView;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.receivers.ExportReceiver;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.PermissionUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import moxy.InjectViewState;

@InjectViewState
public class DocsWebDavPresenter extends DocsBasePresenter<DocsWebDavView> {

    public static final String TAG = DocsWebDavPresenter.class.getSimpleName();

    private File mTempFile;

    private ExportReceiver mExportReceiver;
    private Disposable mDownloadDisposable;

    public DocsWebDavPresenter() {
        App.getApp().getAppComponent().inject(this);
        mToken = mPreferenceTool.getToken();
        mModelExplorerStack = new ModelExplorerStack();
        mFilteringValue = "";
        mPlaceholderType = PlaceholderViews.Type.NONE;
        mIsContextClick = false;
        mIsFilteringMode = false;
        mIsSelectionMode = false;
        mIsFoldersMode = false;
        mExportReceiver = new ExportReceiver();
        mFileProvider = getProvider();
    }

    private BaseFileProvider getProvider() {
        AccountsSqlData accountsSqlData = mAccountSqlTool.getAccountOnline();
        if (accountsSqlData != null) {
            return new WebDavFileProvider(WebDavApi.getApi(accountsSqlData.getScheme() + accountsSqlData.getPortal()),
                    WebDavApi.Providers.valueOf(accountsSqlData.getWebDavProvider()));
        } else {
            return null;
        }
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        mExportReceiver.setOnExportReceiver(uri -> upload(uri, null));
        mContext.registerReceiver(mExportReceiver, ExportReceiver.getFilters());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExportReceiver.setOnExportReceiver(null);
        mContext.unregisterReceiver(mExportReceiver);
    }

    @Override
    public void getNextList() {
        // Stub
    }

    @Override
    public void createDocs(@NonNull String title) {
        final String id = mModelExplorerStack.getCurrentId();
        if (id != null) {
            final RequestCreate requestCreate = new RequestCreate();
            requestCreate.setTitle(title);
            mDisposable.add(mFileProvider.createFile(id, requestCreate).subscribe(file -> {
                addFile(file);
                setPlaceholderType(PlaceholderViews.Type.NONE);
                getViewState().onDialogClose();
                getViewState().onOpenLocalFile(file);
            }, this::fetchError));

            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS);
        }
    }

    @Override
    public void getFileInfo() {
        if (mItemClicked != null && mItemClicked instanceof File) {
            final File file = (File) mItemClicked;
            final String extension = file.getFileExst();

            if (StringUtils.isImage(extension)) {
                addRecent(file);
                getViewState().onFileMedia(removeVideo(getListMedia(file.getId())), true);
                return;
            }
        }

        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD);
        mDownloadDisposable = mFileProvider.fileInfo(mItemClicked)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                            mTempFile = file;
                            getViewState().onDialogClose();
                            getViewState().onOpenLocalFile(file);
                        }
                        , this::fetchError);
    }

    @Override
    public void addRecent(@NonNull File file) {
        AccountsSqlData account = mAccountSqlTool.getAccountOnline();
        if (account != null) {
            if (StringUtils.isImage(file.getFileExst())) {
                mAccountSqlTool.addRecent(file.getId(), file.getWebUrl(), file.getTitle(), file.getPureContentLength(),
                        false,true, new Date(), account);
            } else {
                mAccountSqlTool.addRecent(file.getViewUrl(), file.getWebUrl(), file.getTitle(), file.getPureContentLength(),
                        false,true, new Date(), account);
            }

        }
//        if (mTempFile != null && !mTempFile.getWebUrl().equals("")) {
//            FileUtils.asyncDeletePath(Uri.parse(mTempFile.getWebUrl()).getPath());
//        }
    }

    @Override
    protected void updateViewsState() {
        if (mIsSelectionMode) {
            getViewState().onStateUpdateSelection(true);
            getViewState().onActionBarTitle(String.valueOf(mModelExplorerStack.getCountSelectedItems()));
            getViewState().onStateAdapterRoot(mModelExplorerStack.isNavigationRoot());
            getViewState().onStateActionButton(false);
        } else if (mIsFilteringMode) {
            getViewState().onActionBarTitle(mContext.getString(R.string.toolbar_menu_search_result));
            getViewState().onStateUpdateFilter(true, mFilteringValue);
            getViewState().onStateAdapterRoot(mModelExplorerStack.isNavigationRoot());
            getViewState().onStateActionButton(false);
        } else if (!mModelExplorerStack.isRoot()) {
            getViewState().onStateAdapterRoot(false);
            getViewState().onStateUpdateRoot(false);
            getViewState().onStateActionButton(true);
            getViewState().onActionBarTitle(getCurrentTitle());
        } else {
            if (mIsFoldersMode) {
                getViewState().onActionBarTitle(mContext.getString(R.string.operation_title));
                getViewState().onStateActionButton(false);
            } else {
                getViewState().onActionBarTitle("");
                getViewState().onStateActionButton(true);
            }

            getViewState().onStateAdapterRoot(true);
            getViewState().onStateUpdateRoot(true);
        }
    }

    @Override
    public void onContextClick(Item item, int position, boolean isTrash) {
        onClickEvent(item, position);
        mIsContextClick = true;

        final ContextBottomDialog.State state = new ContextBottomDialog.State();

        state.mTitle = getItemClickedTitle();
        state.mInfo = TimeUtils.formatDate(getItemClickedDate());
        state.mIsFolder = item instanceof Folder;
        state.mIsWebDav = true;
        state.mIsTrash = isTrash;
        if (!isClickedItemFile()) {
            state.mIconResId = R.drawable.ic_type_folder;
        } else {
            state.mIconResId = getIconContext(StringUtils.getExtensionFromPath(getItemClickedTitle()));
        }
        state.mIsPdf = isPdf();
        if (state.mIsShared && state.mIsFolder) {
            state.mIconResId = R.drawable.ic_type_folder_shared;
        }
        getViewState().onItemContext(state);
    }

    @Override
    public void onActionClick() {
        getViewState().onActionDialog();
    }

    @Override
    public boolean move() {
        if (super.move()) {
            transfer(Api.Operation.DUPLICATE, true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean copy() {
        if (super.copy()) {
            transfer(Api.Operation.DUPLICATE, false);
            return true;
        } else  {
            return false;
        }
    }

    @Override
    public void upload(@Nullable Uri uri, @Nullable ClipData uris) {
        if (mPreferenceTool.getUploadWifiState() && !NetworkUtils.isWifiEnable(mContext)) {
            getViewState().onSnackBar(mContext.getString(R.string.upload_error_wifi));
            return;
        }

        final String id = mModelExplorerStack.getCurrentId();
        if (id != null) {
            List<Uri> uploadUris = new ArrayList<>();

            if (uri != null) {
                uploadUris.add(uri);
            }

            if (uris != null && uris.getItemCount() > 0) {
                for (int i = 0; i < uris.getItemCount(); i++) {
                    uploadUris.add(uris.getItemAt(i).getUri());
                }
            }

            uploadWebDav(id, uploadUris);
        }
    }

    @Override
    public void uploadToMy(@Nullable Uri uri) {
        if (uri != null) {
            if (mPreferenceTool.getUploadWifiState() && !NetworkUtils.isWifiEnable(mContext)) {
                getViewState().onSnackBar(mContext.getString(R.string.upload_error_wifi));
                return;
            }
            if (ContentResolverUtils.getSize(mContext, uri) > FileUtils.STRICT_SIZE) {
                getViewState().onSnackBar(mContext.getString(R.string.upload_manager_error_file_size));
                return;
            }

            AccountsSqlData sqlData = mAccountSqlTool.getAccountOnline();
            if (sqlData != null && sqlData.isWebDav()) {
                uploadWebDav(sqlData.getWebDavPath(), Collections.singletonList(uri));
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void uploadWebDav(String id, List<Uri> uriList) {
        if (id.charAt(id.length() - 1) != '/') {
            id = id + "/";
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD);
        mUploadDisposable = mFileProvider.upload(id, uriList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> { }, throwable -> {
                            fetchError(throwable);
                            if (mTempFile != null && !mTempFile.getWebUrl().equals("")) {
                                FileUtils.asyncDeletePath(Uri.parse(mTempFile.getWebUrl()).getPath());
                            }
                        },
                        () -> {
                            deleteTempFile();
                            getViewState().onDialogClose();
                            getViewState().onSnackBar(mContext.getString(R.string.upload_manager_complete));
                            ((WebDavFileProvider) mFileProvider).getUploadFile().clear();
                        });
    }

    @Override
    public void terminate() {
        super.terminate();
        if (mDownloadDisposable != null) {
            mDownloadDisposable.dispose();
        }
    }

    public void deleteTempFile() {
        if (mTempFile != null && PermissionUtils.checkReadWritePermission(mContext)) {
            Uri uri = Uri.parse(mTempFile.getWebUrl());
            if (uri.getPath() != null) {
                FileUtils.asyncDeletePath(uri.getPath());
            }
        }
    }

    private Explorer removeVideo(Explorer listMedia) {
        List<File> files = new ArrayList<>();
        for (File file : listMedia.getFiles()) {
            if (StringUtils.isImage(file.getFileExst())) {
                files.add(file);
            }
        }
        listMedia.setFiles(files);
        return listMedia;
    }
}