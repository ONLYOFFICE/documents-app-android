package app.editors.manager.mvp.presenters.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.providers.CloudFileProvider;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.managers.receivers.UploadReceiver;
import app.editors.manager.managers.services.UploadService;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.models.ModelExplorerStack;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestDeleteShare;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestFavorites;
import app.editors.manager.mvp.models.response.ResponseFiles;
import app.editors.manager.mvp.views.main.DocsCloudView;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.dialogs.MoveCopyDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.KeyboardUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import moxy.InjectViewState;
import retrofit2.HttpException;

@InjectViewState
public class DocsCloudPresenter extends DocsBasePresenter<DocsCloudView>
        implements DownloadReceiver.OnDownloadListener, UploadReceiver.OnUploadListener {

    private HashMap<String, Disposable> mGetDisposable = new HashMap<>();

    @Nullable
    private String mExternalAccessType;

    private boolean mIsTrashMode;

    /*
     * Loading receivers
     * */
    private DownloadReceiver mDownloadReceiver;
    private UploadReceiver mUploadReceiver;

    public DocsCloudPresenter() {
        App.getApp().getAppComponent().inject(this);
        mDownloadReceiver = new DownloadReceiver();
        mUploadReceiver = new UploadReceiver();
        mToken = mPreferenceTool.getToken();
        mModelExplorerStack = new ModelExplorerStack();
        mFilteringValue = "";
        mPlaceholderType = PlaceholderViews.Type.NONE;
        mIsContextClick = false;
        mIsFilteringMode = false;
        mIsSelectionMode = false;
        mIsFoldersMode = false;
        mIsTrashMode = false;
        mFileProvider = new CloudFileProvider(mToken, App.getApp().getAppComponent().getRetrofit().getApiWithPreferences());
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        mDownloadReceiver.setOnDownloadListener(this);
        mUploadReceiver.setOnUploadListener(this);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUploadReceiver, mUploadReceiver.getFilter());
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDownloadReceiver, mDownloadReceiver.getFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadReceiver.setOnDownloadListener(null);
        mUploadReceiver.setOnUploadListener(null);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUploadReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDownloadReceiver);
    }

    @Override
    public void onItemClick(Item item, int position) {
        onClickEvent(item, position);
        mIsContextClick = false;

        if (mIsSelectionMode) {
            final boolean isChecked = !mItemClicked.isSelected();
            mModelExplorerStack.setSelectById(item, isChecked);
            if (!isSelectedItemsEmpty()) {
                getViewState().onStateUpdateSelection(true);
                getViewState().onItemSelected(position, String.valueOf(mModelExplorerStack.getCountSelectedItems()));
            }
        } else if (!mIsTrashMode) {
            if (mItemClicked instanceof Folder) {
                openFolder(mItemClicked.getId(), position);
            } else if (mItemClicked instanceof File) {
                getFileInfo();
            }
        } else {
            getViewState().onSnackBarWithAction(mContext.getString(R.string.trash_snackbar_move_text),
                    mContext.getString(R.string.trash_snackbar_move_button), v -> moveContext());
        }
    }

    @Override
    public boolean copy() {
        if (super.copy()) {
            checkMoveCopyFiles(MoveCopyDialog.ACTION_COPY);
            return true;
        }
        return false;
    }

    @Override
    public boolean move() {
        if (super.move()) {
            checkMoveCopyFiles(MoveCopyDialog.ACTION_MOVE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void getNextList() {
        final String id = mModelExplorerStack.getCurrentId();
        final int loadPosition = mModelExplorerStack.getLoadPosition();
        if (id != null && loadPosition > 0) {
            final Map<String, String> args = getArgs(mFilteringValue);

            args.put(Api.Parameters.ARG_START_INDEX, String.valueOf(loadPosition));

            mDisposable.add(mFileProvider.getFiles(id, args).subscribe(explorer -> {
                mModelExplorerStack.addOnNext(explorer);

                Explorer last = mModelExplorerStack.last();
                if (last != null) {
                    getViewState().onDocsNext(getListWithHeaders(last, true));
                }

            }, this::fetchError));
        }
    }

    @Override
    public void createDocs(@NonNull String title) {
        if (mPreferenceTool.getPortal() != null) {
            FirebaseUtils.addAnalyticsCreateEntity(mPreferenceTool.getPortal(), true, StringUtils.getExtensionFromPath(title));
        }

        final String id = mModelExplorerStack.getCurrentId();
        if (id != null) {
            final RequestCreate requestCreate = new RequestCreate();
            requestCreate.setTitle(title);
            mDisposable.add(mFileProvider.createFile(id, requestCreate).subscribe(file -> {

                addFile(file);
                setPlaceholderType(PlaceholderViews.Type.NONE);

                getViewState().onDialogClose();
                getViewState().onCreateFile(file);
            }, this::fetchError));

            showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS);
        }
    }

    @Override
    public void getFileInfo() {
        if (mItemClicked != null) {
            mDisposable.add(mFileProvider.fileInfo(mItemClicked)
                    .subscribe(file -> onFileClickAction(), this::fetchError));
        }
    }

    @Override
    public void addRecent(File file) {
        AccountsSqlData account = mAccountSqlTool.getAccountOnline();
        if (account != null) {
            mAccountSqlTool.addRecent(file.getId(), null, file.getTitle(), file.getPureContentLength(),
                    false, account.isWebDav(), new Date(), account);
        }
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
            getViewState().onStateActionButton(isContextEditable());

        } else if (!mModelExplorerStack.isRoot()) {
            getViewState().onStateAdapterRoot(false);
            getViewState().onStateUpdateRoot(false);
            getViewState().onStateActionButton(isContextEditable());
            getViewState().onActionBarTitle(getCurrentTitle());
        } else {
            if (mIsTrashMode) {
                getViewState().onStateActionButton(false);
            } else if (mIsFoldersMode) {
                getViewState().onActionBarTitle(mContext.getString(R.string.operation_title));
                getViewState().onStateActionButton(false);
            } else {
                getViewState().onActionBarTitle("");
                getViewState().onStateActionButton(isContextEditable());
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
        state.mIsFolder = !isClickedItemFile();
        state.mIsShared = isClickedItemShared();
        state.mIsCanShare = isItemShareable();
        state.mIsDocs = isClickedItemDocs();
        state.mIsContextEditable = isContextItemEditable();
        state.mIsItemEditable = isItemEditable();
        state.mIsStorage = isClickedItemStorage() && isRoot();
        state.mIsDeleteShare = isShareSection();
        state.mIsWebDav = false;
        state.mIsTrash = isTrash;
        state.mIsFavorite = isClickedItemFavorite();
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
        getViewState().onActionDialog(isRoot() && (isUserSection() || (isCommonSection() && isAdmin())), !isVisitor());
    }

    /*
     * Loading callbacks
     * */
    @Override
    public void onDownloadError(String id, String url, String title, String info) {
        getViewState().onDialogClose();
        getViewState().onSnackBar(info);
    }

    @Override
    public void onDownloadProgress(String id, int total, int progress) {
        getViewState().onDialogProgress(total, progress);
    }

    @Override
    public void onDownloadComplete(String id, String url, String title, String info, String path, String mime) {
        getViewState().onDialogClose();
        getViewState().onSnackBarWithAction(info + "\n" + title, mContext.getString(R.string.download_manager_open),
                v -> showDownloadFolderActivity());
    }

    @Override
    public void onDownloadCanceled(String id, String info) {
        getViewState().onDialogClose();
        getViewState().onSnackBar(info);
    }

    @Override
    public void onDownloadRepeat(String id, String title, String info) {
        getViewState().onDialogClose();
        getViewState().onSnackBar(info);
    }

    @Override
    public void onUploadError(@Nullable String path, String info, UploadFile file) {
        getViewState().onSnackBar(info);
        getViewState().onDeleteUploadFile(file.getId());
    }

    @Override
    public void onUploadComplete(String path, String info, @Nullable String title, File file, String id) {
        getViewState().onSnackBar(info);
        if (mModelExplorerStack.getCurrentId().equals(file.getFolderId())) {
            addFile(file);
        }
        getViewState().onDeleteUploadFile(id);
    }

    @Override
    public void onUploadAndOpen(String path, @Nullable String title, File file, String id) {
        getViewState().onFileWebView(file);
    }

    @Override
    public void onUploadFileProgress(int progress, String id, String folderId) {
        if (mModelExplorerStack.getCurrentId().equals(folderId)) {
            getViewState().onUploadFileProgress(progress, id);
        }
    }


    @Override
    public void onUploadCanceled(String path, String info, String id) {
        getViewState().onSnackBar(info);
        getViewState().onDeleteUploadFile(id);
        if (UploadService.getUploadFiles(mModelExplorerStack.getCurrentId()).isEmpty()) {
            getViewState().onRemoveUploadHead();
            getListWithHeaders(mModelExplorerStack.last(), true);
        }
    }

    @Override
    public void onUploadRepeat(String path, String info) {
        getViewState().onDialogClose();
        getViewState().onSnackBar(info);
    }

    public void onEditContextClick() {
        if (mItemClicked instanceof File) {
            final File file = (File) mItemClicked;
            file.setReadOnly(false);
            String url = file.getWebUrl();
            if (url.contains(Api.Parameters.ARG_ACTION) && url.contains(Api.Parameters.VAL_ACTION_VIEW)) {
                url = url.substring(0, url.indexOf('&'));
                file.setWebUrl(url);
            }
            addRecent(file);
            getViewState().onFileWebView(file);
        }
    }

    public void removeShareSelected() {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            final RequestDeleteShare deleteShare = new RequestDeleteShare();
            deleteShare.setFolderIds(mModelExplorerStack.getSelectedFoldersIds());
            deleteShare.setFileIds(mModelExplorerStack.getSelectedFilesIds());

            mDisposable.add(Observable.fromCallable(() -> mRetrofitTool.getApiWithPreferences()
                    .deleteShare(mToken, deleteShare).execute())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseResponse -> {
                        mModelExplorerStack.removeSelected();
                        resetDatesHeaders();
                        setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
                        getViewState().onActionBarTitle("0");
                        getViewState().onDeleteBatch(getListWithHeaders(mModelExplorerStack.last(), true));
                        onBatchOperations();
                    }, this::fetchError));
        }
    }

    public void removeShare() {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            getViewState().onDialogQuestion(mContext.getString(R.string.dialogs_question_share_remove), null,
                    TAG_DIALOG_ACTION_REMOVE_SHARE);
        } else {
            getViewState().onSnackBar(mContext.getString(R.string.operation_empty_lists_data));
        }
    }

    public void getExternalLink() {
        if (mItemClicked != null) {
            mExternalAccessType = Api.ShareType.READ;
            final RequestExternal requestExternal = new RequestExternal();
            requestExternal.setShare(mExternalAccessType);
            mDisposable.add(mFileProvider.share(mItemClicked.getId(), requestExternal)
                    .subscribe(responseExternal -> {
                        mItemClicked.setShared(!mItemClicked.getShared());
                        switch (mExternalAccessType) {
                            case Api.ShareType.NONE:
                                getViewState().onDocsAccess(false, mContext.getString(R.string.share_access_denied));
                                break;
                            case Api.ShareType.READ:
                            case Api.ShareType.READ_WRITE:
                            case Api.ShareType.REVIEW:
                                KeyboardUtils.setDataToClipboard(mContext, responseExternal.getResponse(), mContext.getString(R.string.share_clipboard_external_link_label));
                                getViewState().onDocsAccess(true, mContext.getString(R.string.share_clipboard_external_copied));
                                break;
                        }
                    }, this::fetchError));
        }
    }

    public void addToFavorite() {
        final RequestFavorites requestFavorites = new RequestFavorites();
        requestFavorites.setFileIds(new ArrayList<String>(Collections.singletonList(mItemClicked.getId())));
        mDisposable.add(mFileProvider.addToFavorites(requestFavorites)
        .subscribe(response -> {
            mItemClicked.setFavorite(!mItemClicked.getFavorite());
            getViewState().onSnackBar(mContext.getString(R.string.operation_add_to_favorites));
        }, this::fetchError));
    }

    public void deleteFromFavorite() {
        final RequestFavorites requestFavorites = new RequestFavorites();
        requestFavorites.setFileIds(new ArrayList<String>(Collections.singletonList(mItemClicked.getId())));
        mDisposable.add(mFileProvider.deleteFromFavorites(requestFavorites)
        .subscribe(response -> {
            mItemClicked.setFavorite(!mItemClicked.getFavorite());
            getViewState().onRemoveItemFromFavorites();
            getViewState().onSnackBar(mContext.getString(R.string.operation_remove_from_favorites));
        }, this::fetchError));
    }


    public void removeFromFavorites() {
        if (mItemClicked != null) {
            mModelExplorerStack.removeItemById(mItemClicked.getId());
        }
        getViewState().onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true));
    }

    public void removeShareContext() {
        if (mItemClicked != null) {
            final RequestDeleteShare deleteShare = new RequestDeleteShare();

            if (mItemClicked instanceof Folder) {
                deleteShare.setFolderIds(new ArrayList<>(Collections.singletonList(mItemClicked.getId())));
            } else {
                deleteShare.setFileIds(new ArrayList<>(Collections.singletonList(mItemClicked.getId())));
            }

            mDisposable.add(Observable.fromCallable(() -> mRetrofitTool.getApiWithPreferences()
                    .deleteShare(mToken, deleteShare).execute())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseResponse -> {
                        if (mItemClicked != null) {
                            mModelExplorerStack.removeItemById(mItemClicked.getId());
                        }
                        setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
                        getViewState().onDocsGet(getListWithHeaders(mModelExplorerStack.last(), true));
                        onBatchOperations();
                    }, this::fetchError));
        }
    }

    public void setTrashMode(boolean trashMode) {
        if (mIsTrashMode = trashMode) {
//            getViewState().onActionBarTitle(mContext.getString(R.string.main_pager_docs_trash));
        }
    }

    public void emptyTrash() {
        final Explorer explorer = mModelExplorerStack.last();
        if (explorer != null) {

            CloudFileProvider provider = (CloudFileProvider) mFileProvider;
            mDisposable.add(provider.clearTrash()
                    .doOnSubscribe(disposable -> showDialogWaiting(TAG_DIALOG_CANCEL_BATCH_OPERATIONS))
                    .subscribe(response -> {
                                onBatchOperations();
                                refresh();
                            },
                            this::fetchError
                    ));
        }

    }

    private void checkMoveCopyFiles(String action) {
        List<String> filesIds = mOperationStack.getSelectedFilesIds();
        List<String> foldersIds = mOperationStack.getSelectedFoldersIds();

        mDisposable.add(mRetrofitTool.getApiWithPreferences()
                .checkFiles(mPreferenceTool.getToken(), mDestFolderId, foldersIds, filesIds)
                .map(ResponseFiles::getResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(files -> {
                    if (files.size() != 0) {
                        showMoveCopyDialog(files, action, mModelExplorerStack.getCurrentTitle());
                    } else if (action.equals(MoveCopyDialog.ACTION_COPY)) {
                        transfer(Api.Operation.DUPLICATE, false);
                    } else if (action.equals(MoveCopyDialog.ACTION_MOVE)) {
                        transfer(Api.Operation.DUPLICATE, true);
                    }
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        HttpException exception = (HttpException) throwable;
                        if (exception.code() == Api.HttpCodes.CLIENT_FORBIDDEN) {
                            getViewState().onError(mContext.getString(R.string.errors_client_forbidden));
                        }
                    } else {
                        onFailureHandle(throwable);
                    }
                }));
    }

    private void showMoveCopyDialog(List<File> files, String action, String titleFolder) {
        ArrayList<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getTitle());
        }
        getViewState().showMoveCopyDialog(names, action, titleFolder);
    }

    private void onFileClickAction() {
        if (mItemClicked instanceof File) {

            final File file = (File) mItemClicked;
            final String extension = file.getFileExst();

            // Add action here for various extensions
            switch (StringUtils.getExtension(extension)) {
                case DOC:
                case SHEET:
                case PRESENTATION:
                case PDF:
                    addRecent((File) mItemClicked);
                    file.setReadOnly(true);
                    getViewState().onFileWebView(file);
                    break;
                case IMAGE:
                case IMAGE_GIF:
                case VIDEO_SUPPORT:
                    addRecent((File) mItemClicked);
                    getViewState().onFileMedia(getListMedia(file.getId()), false);
                    break;
                default:
                    getViewState().onFileDownloadPermission();
            }

            if (mPreferenceTool.getPortal() != null) {
                FirebaseUtils.addAnalyticsOpenEntity(mPreferenceTool.getPortal(), extension);
            }
        }
    }

    private void cancelRequest(String id) {
        if (mGetDisposable.containsKey(id)) {
            Disposable disposable = mGetDisposable.remove(id);
            if (disposable != null) {
                disposable.dispose();
            }
        }
    }

    /*
     * Getter/Setters for states
     * */

    private boolean isAdmin() {
        return mPreferenceTool.getIsAdmin();
    }

    private boolean isVisitor() {
        return mPreferenceTool.getIsVisitor();
    }

    /*
     * A&(B&(Cv(D&!E)))v((FvGvH)&D&!E)
     * */
    private boolean isContextEditable() {
        return isUserSection() || (isCommonSection() && (isAdmin() || (isContextReadWrite() && !isRoot()))) ||
                ((isShareSection() || isProjectsSection() || isBunchSection()) && isContextReadWrite() && !isRoot());
    }

    /*
     * I&(!K&!F&!BvJ)
     * */
    public boolean isContextItemEditable() {
        return isContextEditable() && (!isVisitor() && !isShareSection() || isCommonSection() || isItemOwner());
    }

    private boolean isContextOwner() {
        return StringUtils.equals(mModelExplorerStack.getCurrentFolderOwnerId(), mPreferenceTool.getSelfId());
    }

    private boolean isContextReadWrite() {
        return isContextOwner() || mModelExplorerStack.getCurrentFolderAccess() == Api.ShareCode.READ_WRITE ||
                mModelExplorerStack.getCurrentFolderAccess() == Api.ShareCode.NONE;
    }

    public boolean isUserSection() {
        return mModelExplorerStack.getRootFolderType() == Api.SectionType.CLOUD_USER;
    }

    private boolean isShareSection() {
        return mModelExplorerStack.getRootFolderType() == Api.SectionType.CLOUD_SHARE;
    }

    private boolean isCommonSection() {
        return mModelExplorerStack.getRootFolderType() == Api.SectionType.CLOUD_COMMON;
    }

    private boolean isProjectsSection() {
        return mModelExplorerStack.getRootFolderType() == Api.SectionType.CLOUD_PROJECTS;
    }

    private boolean isBunchSection() {
        return mModelExplorerStack.getRootFolderType() == Api.SectionType.CLOUD_BUNCH;
    }

    private boolean isClickedItemShared() {
        return mItemClicked != null && mItemClicked.getShared();
    }

    private boolean isClickedItemFavorite() {
        return mItemClicked != null && mItemClicked.getFavorite();
    }

    private boolean isItemOwner() {
        return mItemClicked != null && StringUtils.equals(mItemClicked.getCreatedBy().getId(), mPreferenceTool.getSelfId());
    }

    private boolean isItemReadWrite() {
        return mItemClicked != null && (mItemClicked.getAccess() == Api.ShareCode.READ_WRITE ||
                mItemClicked.getAccess() == Api.ShareCode.NONE ||
                mItemClicked.getAccess() == Api.ShareCode.REVIEW);
    }

    private boolean isItemEditable() {
        return !isVisitor() && !isProjectsSection() && (isItemOwner() || isItemReadWrite());
    }

    private boolean isItemShareable() {
        return isItemEditable() && (!isCommonSection() || isAdmin()) && !mPreferenceTool.isPersonalPortal() && !isProjectsSection() && !isBunchSection();
    }

    private boolean isClickedItemStorage() {
        return mItemClicked != null && mItemClicked.getProviderItem();
    }

    public boolean isTrashMode() {
        return mIsTrashMode;
    }

    private void showDownloadFolderActivity() {
        getViewState().onDownloadActivity();
    }
}
