package app.editors.manager.mvp.presenters.main;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.providers.LocalFileProvider;
import app.editors.manager.managers.providers.ProviderError;
import app.editors.manager.managers.providers.WebDavFileProvider;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.models.ModelExplorerStack;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.views.main.DocsOnDeviceView;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.tools.LocalContentTools;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.PathUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import moxy.InjectViewState;

@InjectViewState
public class DocsOnDevicePresenter extends DocsBasePresenter<DocsOnDeviceView> {

    public static final String TAG = DocsOnDevicePresenter.class.getSimpleName();

    @Nullable
    private Uri mPhotoUri;
    private WebDavFileProvider mWebDavFileProvider;

    public DocsOnDevicePresenter() {
        App.getApp().getAppComponent().inject(this);
        mToken = mPreferenceTool.getToken();
        mModelExplorerStack = new ModelExplorerStack();
        mFilteringValue = "";
        mPlaceholderType = PlaceholderViews.Type.NONE;
        mIsContextClick = false;
        mIsFilteringMode = false;
        mIsSelectionMode = false;
        mIsFoldersMode = false;
        mFileProvider = new LocalFileProvider(new LocalContentTools(mContext));
        checkWebDav();
    }

    private void checkWebDav() {
        if (mAccountSqlTool.getAccountOnline() != null && mAccountSqlTool.getAccountOnline().isWebDav()) {
            final AccountsSqlData account = mAccountSqlTool.getAccountOnline();
            mWebDavFileProvider = new WebDavFileProvider(WebDavApi.getApi(account.getScheme() + account.getPortal()),
                    WebDavApi.Providers.valueOf(account.getWebDavProvider()));
        }
    }

    @Override
    public void getNextList() {
        // Stub to local
    }

    @Override
    public void createDocs(@NonNull String title) {
        final String id = mModelExplorerStack.getCurrentId();
        if (id != null) {
            RequestCreate requestCreate = new RequestCreate();
            requestCreate.setTitle(title);
            mDisposable.add(mFileProvider.createFile(id, requestCreate)
                    .subscribe(file -> {
                        addFile(file);
                        addRecent(file);
                        openFile(file);
                    }, throwable -> getViewState().onError(mContext.getString(R.string.errors_create_local_file))));
        }

    }

    @Override
    public void getFileInfo() {
        if (mItemClicked != null && mItemClicked instanceof File) {
            File file = (File) mItemClicked;
            addRecent(file);
            openFile(file);
        }
    }

    @Override
    public void addRecent(File file) {
        mAccountSqlTool.addRecent(null, file.getId(), file.getTitle(), file.getPureContentLength(), true, false, new Date(), null);
    }

    public void addRecent(Uri uri) {
        final DocumentFile file = DocumentFile.fromSingleUri(mContext, uri);
        if (file != null && file.exists()) {
            mAccountSqlTool.addRecent(null, uri.toString(), file.getName(), file.length(), true, false, new Date(), null);
        }
    }

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
                getViewState().onActionBarTitle(mContext.getString(R.string.fragment_on_device_title));
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

        ContextBottomDialog.State state = new ContextBottomDialog.State();
        state.mIsLocal = true;
        state.mTitle = item.getTitle();
        state.mInfo = TimeUtils.formatDate(getItemClickedDate());
        state.mIsFolder = item instanceof Folder;
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
    public void deleteItems() {
        List<Item> items = new ArrayList<>();

        List<File> files = mModelExplorerStack.getSelectedFiles();
        List<Folder> folders = mModelExplorerStack.getSelectedFolders();

        items.addAll(folders);
        items.addAll(files);

        mDisposable.add(mFileProvider.delete(items, null)
                .subscribe(operations -> {
                }, throwable -> {
                }, () -> {
                    mModelExplorerStack.removeSelected();
                    getBackStack();
                    setPlaceholderType(mModelExplorerStack.isListEmpty() ? PlaceholderViews.Type.EMPTY : PlaceholderViews.Type.NONE);
                    getViewState().onRemoveItems(items);
                    getViewState().onSnackBar(mContext.getString(R.string.operation_complete_message));
                }));

    }

    @Override
    public void uploadToMy(Uri uri) {
        if (mWebDavFileProvider != null) {
            final String id = mAccountSqlTool.getAccountOnline().getWebDavPath();
            uploadWebDav(id, Collections.singletonList(uri));
        } else {
            super.uploadToMy(uri);
        }
    }

    private void uploadWebDav(String id, List<Uri> uriList) {
        if (id.charAt(id.length() - 1) != '/') {
            id = id + "/";
        }
        mUploadDisposable = mWebDavFileProvider.upload(id, uriList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                        },
                        this::fetchError,
                        () -> {
                            getViewState().onDialogClose();
                            getViewState().onSnackBar(mContext.getString(R.string.upload_manager_complete));
                            for (File file : ((WebDavFileProvider) mFileProvider).getUploadFile()) {
                                addFile(file);
                            }
                            ((WebDavFileProvider) mFileProvider).getUploadFile().clear();
                        });
    }

    @Override
    public boolean sortBy(@NonNull String value, boolean isRepeatedTap) {
        mPreferenceTool.setSortBy(value);

        if(isRepeatedTap) {
            reverseSortOrder();
        }

        getItemsById(mModelExplorerStack.getCurrentId());
        return true;
    }

    @Override
    public boolean orderBy(@NonNull String value) {
        mPreferenceTool.setSortOrder(value);
        getItemsById(mModelExplorerStack.getCurrentId());
        return true;
    }

    @Override
    public void rename(@Nullable String title) {
        final Item item = mModelExplorerStack.getItemById(mItemClicked);
        if (item != null) {
            java.io.File existFile = new java.io.File(item.getId());
            if (existFile.exists()) {
                StringBuilder path = new StringBuilder();
                path.append(existFile.getParent()).append("/").append(title);
                if (item instanceof File) {
                    path.append(((File) item).getFileExst());
                }
                java.io.File renameFile = new java.io.File(path.toString());
                if (renameFile.exists()) {
                    getViewState().onError(mContext.getString(R.string.rename_file_exist));
                } else {
                    super.rename(title);
                }
            }
        }
    }

    public void moveFile(Uri data, boolean isCopy) {
        String path = PathUtils.getFolderPath(mContext, data);
        if (isSelectionMode()) {
            moveSelection(path, isCopy);
            return;
        }
        try {
            if (((LocalFileProvider) mFileProvider).transfer(path, mItemClicked, isCopy)) {
                refresh();
                getViewState().onSnackBar(mContext.getString(R.string.operation_complete_message));
            } else {
                getViewState().onError(mContext.getString(R.string.operation_error_move_to_same));
            }
        } catch (Exception e) {
            catchTransferError(e);
        }
    }

    public void openFromChooser(Uri uri) {
        final String fileName = ContentResolverUtils.getName(mContext, uri);
        final String ext = StringUtils.getExtensionFromPath(fileName.toLowerCase());
        addRecent(uri);
        openFile(uri, ext);
    }

    private void openFile(File file) {
        String path = file.getId();
        Uri uri = Uri.fromFile(new java.io.File(path));
        String ext = StringUtils.getExtensionFromPath(file.getId().toLowerCase());
        openFile(uri, ext);
    }

    private void openFile(Uri uri, String ext) {
        switch (StringUtils.getExtension(ext)) {
            case DOC:
                getViewState().onShowDocs(uri);
                break;
            case SHEET:
                getViewState().onShowCells(uri);
                break;
            case PRESENTATION:
                getViewState().onShowSlides(uri);
                break;
            case PDF:
                getViewState().onShowPdf(uri);
                break;
            case IMAGE:
            case IMAGE_GIF:
            case VIDEO_SUPPORT:
                showMedia();
                break;
            default:
                getViewState().onError(mContext.getString(R.string.error_unsupported_format));
                break;
        }
    }

    private void moveSelection(String path, boolean isCopy) {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {

            if (mFileProvider instanceof LocalFileProvider) {
                LocalFileProvider provider = (LocalFileProvider) mFileProvider;

                List<Item> items = new ArrayList<>();

                List<File> files = mModelExplorerStack.getSelectedFiles();
                List<Folder> folders = mModelExplorerStack.getSelectedFolders();

                items.addAll(folders);
                items.addAll(files);

                for (Item item : items) {
                    try {
                        if (!provider.transfer(path, item, isCopy)) {
                            getViewState().onError(mContext.getString(R.string.operation_error_move_to_same));
                            break;
                        }
                    } catch (Exception e) {
                        catchTransferError(e);
                    }
                }
                getBackStack();
                refresh();
                getViewState().onSnackBar(mContext.getString(R.string.operation_complete_message));
            }

        } else {
            getViewState().onError(mContext.getString(R.string.operation_empty_lists_data));
        }
    }

    public void showDeleteDialog() {
        if (mItemClicked != null) {
            if (mItemClicked instanceof Folder) {
                getViewState().onDialogQuestion(mContext.getString(R.string.dialogs_question_delete),
                        mContext.getString(R.string.dialog_question_delete_folder),
                        TAG_DIALOG_DELETE_CONTEXT);
            } else {
                getViewState().onDialogQuestion(mContext.getString(R.string.dialogs_question_delete),
                        mContext.getString(R.string.dialog_question_delete_file),
                        TAG_DIALOG_DELETE_CONTEXT);
            }
        }
    }

    public void deleteFile() {
        if (mItemClicked != null) {
            List<Item> items = new ArrayList<>();
            items.add(mItemClicked);
            mDisposable.add(mFileProvider.delete(items, null)
                    .subscribe(operations -> {
                    }, throwable -> {
                    }, () -> {
                        mModelExplorerStack.removeItemById(mItemClicked.getId());
                        getViewState().onRemoveItem(mItemClicked);
                        getViewState().onSnackBar(mContext.getString(R.string.operation_complete_message));
                    }));
        }
    }

    @SuppressLint("MissingPermission")
    public void createPhoto() {
        java.io.File photo = FileUtils.createFile(new java.io.File(getStack().getCurrent().getId()), TimeUtils.getFileTimeStamp(), "png");
        if (photo != null) {
            mPhotoUri = ContentResolverUtils.getFileUri(mContext, photo);
            getViewState().onShowCamera(mPhotoUri);
        }

    }

    public void deletePhoto() {
        if (mPhotoUri != null) {
            mContext.getContentResolver().delete(mPhotoUri, null, null);
        }
    }

    public void checkSelectedFiles() {
        if (mModelExplorerStack.getCountSelectedItems() > 0) {
            getViewState().onShowFolderChooser();
        } else {
            getViewState().onError(mContext.getString(R.string.operation_empty_lists_data));
        }
    }

    public void upload() {
        if (mItemClicked != null) {
            Uri uri = Uri.fromFile(new java.io.File(mItemClicked.getId()));
            if (uri != null) {
                uploadToMy(uri);
            }
        }
    }

    public void showMedia() {
        getViewState().onOpenMedia(getMediaFile());
    }

    private Explorer getMediaFile() {
        Explorer explorer = mModelExplorerStack.last().clone();
        if (explorer != null) {
            List<File> files = explorer.getFiles();
            List<File> images = new ArrayList<>();
            for (File file : files) {
                String extension = file.getFileExst();
                if (StringUtils.isImage(extension) || StringUtils.isImageGif(extension) || StringUtils.isVideoSupport(extension)) {
                    File cloneFile = file.clone();
                    cloneFile.setId("");
                    images.add(cloneFile);
                }
                if (file.equals(mItemClicked)) {
                    file.setClicked(true);
                }
            }

            explorer.setFolders(null);
            explorer.setFiles(images);
            return explorer;
        }
        return new Explorer();
    }

    @Override
    protected void fetchError(Throwable throwable) {
        if (throwable.getMessage() != null) {
            if (throwable.getMessage().equals(ProviderError.ERROR_CREATE_LOCAL)) {
                getViewState().onError(mContext.getString(R.string.rename_file_exist));
            } else {
                super.fetchError(throwable);
            }
        }
    }

    private void catchTransferError(Exception e) {
        if (e.getMessage() != null) {
            switch (e.getMessage()) {
                case ProviderError.FILE_EXIST:
                    getViewState().onError(mContext.getString(R.string.operation_error_move_to_same));
                    break;
                case ProviderError.UNSUPPORTED_PATH:
                    getViewState().onError(mContext.getString(R.string.error_unsupported_path));
                    break;
            }
        } else {
            Log.e(TAG, "Error move/copy local");
        }
    }
}